# Trial AI

Trial AI is a JavaFX narrative game built as an academic group project. The game combines courtroom-style investigation, timed decisions, puzzle elements, AI-assisted dialogue, and text-to-speech features to create an interactive mystery experience.

This repository was migrated with full Git commit and author history preserved to accurately reflect the team contributions.

## Features

- JavaFX-based interactive game interface
- AI-assisted dialogue and response generation
- Text-to-speech support in the original project setup
- Timed gameplay interactions
- Puzzle and investigation-style game flow
- Branching user-facing gameplay logic
- Maven-based build and run workflow

## Tech Stack

- Java
- JavaFX
- Maven
- Git / GitHub
- API-based AI integration
- Text-to-speech configuration

## Running the Project

The original course-provided API tokens have been disabled and are not included in this repository.

The project still contains the original API proxy integration used during development. To run the AI-related features, you will need to either:

1. provide a compatible local `apiproxy.config` for your own environment, or
2. refactor the API layer to call the OpenAI API directly using your own API key.

Do not commit or publish any API keys, tokens, or local config files.

To run the game:

```bash
./mvnw clean javafx:run
```

On Windows:

```bash
.\mvnw.cmd clean javafx:run
```

To debug with the existing JavaFX debug configuration:

```bash
./mvnw clean javafx:run@debug
```

Then use the JavaFX debug configuration in VS Code.

## Contributors

This project was developed as a team project by:

- [@yd35](https://github.com/yd35)
- [@miketomotimo](https://github.com/miketomotimo)
- [@LouisCao1029](https://github.com/LouisCao1029)

## Notes

This project was originally built in an academic setting and may include structure or setup patterns from the original project environment. Some external services used during development, including the original text-to-speech setup, may no longer work without replacement or refactoring.

## License

No open-source license is currently provided for this repository.

This project is shared publicly for portfolio and educational viewing purposes only. It includes academic project structure and team contributions, so please do not copy, redistribute, or reuse the code without permission.
