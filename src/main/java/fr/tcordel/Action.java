package fr.tcordel;

public enum Action {
    UP, DOWN, LEFT, RIGHT;

	public static Action from(char c) {
		return switch (c) {
			case 'U' -> UP;
			case 'D' -> DOWN;
			case 'L' -> LEFT;
			case 'R' -> RIGHT;
			default -> throw new IllegalArgumentException("Unexpected value: " + c);
		};
	}
}
