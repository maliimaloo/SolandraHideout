# Plugin SolandraHideout

## Présentation

Le **SolandraHideout** est un plugin Minecraft conçu pour gérer les repaires (hideouts) des gangs dans le jeu. Il offre des fonctionnalités pour créer, gérer et améliorer les repaires, ainsi que pour gérer les mines et les ressources associées à ces repaires.

## Fonctionnalités

- **Gestion des Hideouts** : Créez, chargez et gérez les hideouts associés aux gangs.
- **Améliorations de Mines** : Gérez l'amélioration des mines au sein des repaires, y compris le remplissage des blocs en fonction du niveau de la mine.
- **Opérations Asynchrones sur la Base de Données** : Gestion asynchrone des opérations sur la base de données pour assurer un gameplay fluide sans interruptions.
- **Intégration avec WorldEdit** : Utilise WorldEdit pour placer des schématiques et gérer les régions au sein des repaires.

## Installation

1. Téléchargez la dernière version du plugin.
2. Placez le fichier `.jar` dans le répertoire `plugins` de votre serveur.
3. Redémarrez votre serveur pour charger le plugin.

## Commandes

| Commande                      | Description                                           | Permission               |
|-------------------------------|-------------------------------------------------------|--------------------------|
| `/hideout`                    | Ouvre le menu de gestion d'un hideout.                | `none`                   |
| `/hideoutadmin show <player>` | Ouvre le menu de gestion d'un hideout pour un joueur. | `solandra.hideout.admin` |
| `/hideoutadmin list`          | Donne la liste d'id des hideouts.                     | `solandra.hideout.admin` |
| `/hideout teleport <id>`      | Télèporte le joueur à l'hideout en question.          | `solandra.hideout.admin` |

## Configuration

Le plugin peut être configuré via le fichier `config.yml` situé dans le dossier du plugin. Voici quelques options de configuration :

- **Niveau Max de la Mine** : Définir le niveau maximum que peut atteindre une mine.
- **Cooldown d'Amélioration** : Définir le délai minimum entre deux améliorations d'une mine.

## Dépendances

Ce plugin nécessite les plugins suivants pour fonctionner correctement :
- **WorldEdit** : Pour la gestion des schématiques et des régions.
- **GangsPlugin** : Pour la gestion des gangs.

## Support

Pour toute question ou problème, veuillez ouvrir une issue sur notre [dépôt GitHub](https://github.com/maliimaloo/SolandraHideout) ou rejoindre notre serveur Discord.

## Auteurs

- **maliimaloo** - Développeur principal

## Licence

Ce projet est sous licence MIT. Consultez le fichier `LICENSE` pour plus de détails.
