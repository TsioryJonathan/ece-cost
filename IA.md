# IA.md — Ma façon de travailler avec l'IA

## Outils
- Claude Code (Claude Opus 5.5, en terminal) : brainstorming, rédaction du contrat OpenAPI, découpage en jalons, développement du front React, rédaction de la doc.
- Backend (Spring Boot) : écrit par moi.

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

## Méthode
- Lecture de l'énoncé par l'IA, puis proposition de schéma de BDD et d'hypothèses ; j'ai comparé avec le schéma que j'avais en tête avant de valider.
- Contrat OpenAPI écrit d'abord (`docs/openapi.yaml`) pour travailler en parallèle : moi sur le back, l'IA sur le front, avec un client TypeScript généré depuis ce contrat.
- Travail découpé en jalons (`docs/MILESTONES.md`) avec un responsable par jalon.
- Vérification : <à compléter>

## Exemple de proposition corrigée ou refusée
<à compléter>
