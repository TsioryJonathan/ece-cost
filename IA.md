# IA.md — Ma façon de travailler avec l'IA

## Outils
- Claude Code (Claude Opus 5.5, en terminal) : brainstorming, rédaction du contrat OpenAPI, découpage en jalons, développement du front React, rédaction de la doc.
- Backend (Spring Boot) : écrit par moi, sauf les DTO (objets de requête et de réponse de l'API), écrits par l'IA à partir du contrat `docs/openapi.yaml`, puis relus et intégrés par moi.

## Fichiers de contexte donnés à l'IA
- `CLAUDE.md` (règles du projet : langue, commits, tests, pas de surcharge de librairies, je décide)
- `Use case — Chiffrage.pdf` (l'énoncé)
- `docs/openapi.yaml`, `docs/design.md`, `docs/MILESTONES.md` (produits avec l'IA, validés par moi)

## Prompts principaux (copiés tels quels)

### 1. Cadrage
```
There is a pdf in this folder covering some work i should do within 2 hours, and a mention of an IA.md that we'll take care of from now on so it always stay updated, firstly i wanna discuss and brainstorm our approach concerning all functionnality and work that should be delivered, I already have in mind a db schema and how we are gonna do it but I want you to tell me your version so I can decide, we'll fix up the rule from the start, when commiting, conventionnal commit autored by me, in english without description, everything will be in English except those that are explicitely mentionned to be in French in the pdf, there will always be test, and no overkill when choosing framework or librairies. And most of all, Ill always be the one deciding at the end
```

### 2. Contrat d'API, répartition et jalons
```
you make them, and your schema is the same i had in mind, so to begin ill let you write the oas documentation so we both can work in parallel, you on the frontend and me on the backend, you should use a client api generator from the OAS, and there is already a skill for anti-slop design, and before beggining, we should split work in milestone so you and i can track everything, ill also review your code at the end
```

### 3. Lancement du front en parallèle
```
so you can begin yours now while i do mine ? use a mock server if you have to test if i dont finish yet so you dont have to wait for me, for the client generator, use openapi-typescript with fetch, and just make a clean layout layered with hierarchy, and lastly dont worry about the remote ill create it after
```

## Méthode
- Lecture de l'énoncé par l'IA, puis proposition de schéma de BDD et d'hypothèses ; j'ai comparé avec le schéma que j'avais en tête avant de valider.
- Contrat OpenAPI écrit d'abord (`docs/openapi.yaml`) pour travailler en parallèle : moi sur le back (avec les DTO générés par l'IA d'après le contrat), l'IA sur le front, avec un client TypeScript généré depuis ce contrat (`openapi-typescript` + `openapi-fetch`).
- Travail découpé en jalons (`docs/MILESTONES.md`) avec un responsable par jalon.
- Pour ne pas attendre le back, l'IA a écrit un faux serveur en mémoire (`frontend/mock/server.mjs`, sans dépendance) qui suit le contrat.
- Délégation : Claude a confié l'écriture du faux serveur à un modèle plus petit (Sonnet) avec un cahier des charges précis, puis a relu le code et rejoué les vérifications lui-même.
- Vérification : tests automatisés (Vitest + Testing Library côté front), typecheck `tsc`, captures d'écran à 1440 / 768 / 390 px, puis relecture de tout le code front par moi.

## Exemple de proposition corrigée ou refusée
<à compléter par Jonathan. Candidats notés en cours de route :>
- En relisant le front, j'ai vu que l'IA utilisait `FormEvent`, marqué `@deprecated` dans `@types/react` 19 (« FormEvent doesn't actually exist »). Prompt de relecture : `there is many deprecated type you are using right now, suche as FormProps`. Corrigé en `SubmitEvent<HTMLFormElement>` ; l'IA a ensuite listé toutes les API dépréciées avec le compilateur TypeScript (0 restante) au lieu de corriger seulement celle que j'avais citée.
- Le sous-agent qui a écrit le faux serveur a ajouté de lui-même un en-tête CORS `Access-Control-Allow-Origin: *`, non demandé. Refusé et retiré : le front passe par le proxy Vite, donc pas de CORS, et ça aurait masqué un vrai problème de configuration au moment de brancher le vrai back.
- La première installation a pris TypeScript 7 (réécriture native), incompatible avec `openapi-typescript` qui a besoin de l'API JavaScript de TypeScript 5 : génération en erreur, corrigé en fixant `typescript@5`.
