import type { Difficulty, Mode } from "../types";
import { Field } from "./Field";

type SudokuToolbarProps = {
    mode: Mode;
    difficulty: Difficulty;
};

export function SudokuToolbar({ mode, difficulty }: SudokuToolbarProps) {
    return (
        <section className="toolbar">
            <Field label="Mode">
                <span className={`field-value ${modeValueClassName(mode)}`}>{mode}</span>
            </Field>

            <Field label="Difficulty">
                <span className={`field-value ${difficultyValueClassName(difficulty)}`}>{difficulty}</span>
            </Field>

            <button className="ghost-button toolbar-primary-button" type="button">
                New Game
            </button>
            <button className="toolbar-quit-button" type="button">
                Quit Game
            </button>
        </section>
    );
}

function modeValueClassName(mode: Mode) {
    return mode === "Guided" ? "field-value-mode-guided" : "field-value-mode-classic";
}

function difficultyValueClassName(difficulty: Difficulty) {
    switch (difficulty) {
        case "Easy":
            return "field-value-difficulty-easy";
        case "Medium":
            return "field-value-difficulty-medium";
        case "Hard":
            return "field-value-difficulty-hard";
    }
}
