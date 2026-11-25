# Sous-détail de prix — ECE BTP

Mini-application de chiffrage : une API Spring Boot, une base PostgreSQL et un écran React qui affiche,
pour chaque ouvrage, son déboursé sec et son prix de vente unitaire (déboursé sec × K, K = 1,35).

- `backend/` : API Java 21 / Spring Boot 4, JPA, Flyway (schéma + jeu de données initial).
- `frontend/` : React 19 + TypeScript (Vite), client d'API généré depuis le contrat OpenAPI.
- `docs/openapi.yaml` : le contrat d'API, source de vérité entre le back et le front.
- `docs/design.md`, `docs/MILESTONES.md` : conception et suivi des jalons.
- `IA.md` : ma façon de travailler avec l'IA.

## Lancer l'application

Prérequis : Java 21, Node 22+, PostgreSQL.

### Base de données

```sql
CREATE USER ece WITH PASSWORD 'ece';
CREATE DATABASE ece_cost OWNER ece;
```

Les tables et le jeu de données (4 ouvrages, 7 ressources) sont créés par Flyway au démarrage de l'API.

### API (port 8080)

```bash
cd backend
DB_PASSWORD=ece ./gradlew bootRun
```

Variables possibles (ou fichier `backend/.env`) : `DB_URL` (défaut `jdbc:postgresql://localhost:5432/ece_cost`),
`DB_USER` (défaut `ece`), `DB_PASSWORD`. Le coefficient K est dans `application.properties` (`app.coefficient-k`).

### Front (port 5173)

```bash
cd frontend
npm install
npm run dev          # proxy /api -> http://localhost:8080
```

Sans le back : `npm run mock` (faux serveur en mémoire sur le port 8081, sans dépendance) puis `npm run dev:mock`.

Après une modification de `docs/openapi.yaml` : `npm run generate:api` régénère les types du client.

## Lancer les tests

```bash
cd backend && ./gradlew test      # 52 tests, la base ece_cost doit exister
cd frontend && npm test           # 23 tests, sans back (API simulée)
```

- **Calcul du prix** (`PriceCalculatorTest`, 20 tests) : coût de ligne, déboursé sec = somme des lignes,
  ouvrage sans ressource = 0, prix de vente = déboursé × K arrondi à l'ariary, K lu en configuration,
  prix attendus du jeu de données, un changement de prix se répercute sur tous les ouvrages concernés.
- **Services et mappers** : règles d'ajout (ressource inconnue, déjà présente), mise à jour du prix, tri.
- **API** (`WorkItemApiTest`, MockMvc sur une vraie base, chaque test annulé par transaction) : les 4 routes,
  codes 201 / 400 / 404 / 409, erreurs au format Problem Details.
- **Gestion des erreurs** (`GlobalExceptionHandlerTest`).
- **Front** : règles de saisie (quantité, prix) et comportement de l'écran (affichage des montants, quantité 0
  refusée sans appel API, ajout d'une ressource, message d'erreur de l'API, recalcul après changement de prix).

## Ce qui est fait

- Liste des ouvrages avec déboursé sec, prix de vente unitaire, détail des ressources et répartition
  matériaux / main-d'œuvre / matériel.
- Ajout d'une ressource à un ouvrage avec sa quantité par unité d'ouvrage.
- Modification du prix unitaire d'une ressource ; tous les ouvrages qui la consomment sont recalculés.
- Contrôles de saisie à trois niveaux : écran, API (Bean Validation) et base (contraintes `CHECK` et `UNIQUE`).
- Contrat OpenAPI écrit avant le code, client TypeScript généré depuis ce contrat.

## Ce qui n'est pas fait

- Retirer une ressource d'un ouvrage, modifier une quantité.
- Créer des ouvrages ou des ressources (seulement le jeu de données initial).
- Historique des prix : un nouveau prix remplace l'ancien.
- Tests de bout en bout automatisés dans le dépôt (vérifiés à la main et par script pendant le développement).

## Ce que je ferais ensuite

- Retirer une ressource d'un ouvrage et modifier une quantité (contrat, API, écran).
- Figer les prix par offre : un instantané daté des ressources et des sous-détails (voir « Regard métier »).
- Un test de bout en bout automatisé de l'écran (Playwright) sur les trois parcours.
- Lancer les tests d'API sur une base jetable (Testcontainers) plutôt que sur la base de développement.
- Une intégration continue qui lance les tests back et front, et vérifie que le client généré suit le contrat.

## Hypothèses sur les zones floues

- **Montants et arrondis** : calcul exact (`BigDecimal`), puis coût de ligne, déboursé sec et prix de vente
  arrondis chacun à l'ariary (demi vers le haut) à partir de leur valeur exacte. Le prix de vente est calculé
  sur le déboursé sec exact, pas sur le déboursé arrondi.
- **Prix unitaire** : positif ou nul (une ressource gratuite est possible), 2 décimales au plus.
- **Quantité** : strictement positive, 4 décimales au plus (ex. 0,0125 h de pelle par m²).
- **Ressource déjà présente sur l'ouvrage** : refusée (409) plutôt qu'additionnée, pour éviter une double saisie
  silencieuse.
- **Rien n'est stocké** : déboursé sec et prix de vente sont recalculés à chaque lecture, donc un changement de prix
  se répercute immédiatement sur tous les ouvrages.
- **K** est une constante de configuration, la même pour toute l'application.
- **Ouvrage sans ressource** : déboursé sec et prix de vente à 0.
- **Tri** : ouvrages par identifiant, ressources par nom dans l'ordre alphabétique français (« Maçon » avant
  « Manœuvre »).
- **Langue** : le code, l'API et l'écran sont en anglais ; les données (ouvrages, ressources, unités) sont en
  français comme celles d'une entreprise malgache ; le README et `IA.md` en français, comme demandé.

## Regard métier

Cet écran sert d'abord à l'économiste de la construction qui répond aux appels d'offres : il construit le prix de
chaque ouvrage à partir des ressources et voit tout de suite l'effet d'une hausse du ciment sur toute son offre.
Le commercial s'appuie sur le prix de vente obtenu, et les achats tiennent à jour les prix des ressources.
J'ajouterais ensuite un figement des prix par offre (instantané daté des ressources et des sous-détails), parce
qu'aujourd'hui un nouveau prix écrase l'ancien : on ne peut plus justifier, ni devant le client ni en interne,
sur quelle base une offre déjà remise a été chiffrée.
