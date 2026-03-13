export type Difficulty = "Easy" | "Medium" | "Hard";
export type Mode = "Guided" | "Classic";

export type CellPosition = {
    row: number;
    col: number;
};

export type CellData = CellPosition & {
    value: number | null;
    given: boolean;
    pencilMarks: number[];
};
