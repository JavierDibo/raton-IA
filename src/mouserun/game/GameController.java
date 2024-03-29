/**
 * MouseRun. A programming game to practice building intelligent things.
 * Copyright (C) 2013  Muhammad Mustaqim
 * <p>
 * This file is part of MouseRun.
 * <p>
 * MouseRun is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * MouseRun is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with MouseRun.  If not, see <http://www.gnu.org/licenses/>.
 **/
package mouserun.game;

import mouserun.mouse.*;

import java.util.*;
import java.lang.reflect.*;

/**
 * Class GameController is the brain of the game. It controls the events that occurred 
 * in the game to signal the start and end of the game, the moment a cheese has been 
 * taken and bombs detonated etc. The GameController will communicate to the 
 * Game Interface through the GameControllerAdapter interface.
 */
public class GameController {

    private Maze maze;
    private int gridSize;
    private Cheese cheese;
    private GameControllerAdapter adapter;
    private ArrayList<MouseController> mouseList;
    private ArrayList<Bomb> bombList;
    private int numberOfCheese;
    private int numberOfCheeseTaken;

    /**
     * Creates an instance of the controller.
     * @param adapter The adapter that is responsible for picking up events from the controller and manipulate the game interface.
     * @param width The number of horizontal grids.
     * @param height The number of vertical grids.
     * @param gridSize The width and height of each cell.
     * @param numberOfCheese The number of cheese to play for.
     */
    public GameController(GameControllerAdapter adapter, int width, int height, int gridSize, int numberOfCheese) {
        this.adapter = adapter;
        this.mouseList = new ArrayList<MouseController>();
        this.bombList = new ArrayList<Bomb>();
        this.maze = new Maze(width, height);
        this.gridSize = gridSize;

        this.numberOfCheese = numberOfCheese;
        this.numberOfCheeseTaken = 0;

    }

    /**
     * Retrieve the maze that was generated by the controller.
     * @return The Maze of the game.
     */
    public Maze getMaze() {
        return this.maze;
    }

    /**
     * Causes the game to begin. This will load all instances of Mouse call
     * that conforms to the game set and signal the adapter to prepare for the game
     * to begin.
     */
    public void start() {
        adapter.clearMouse();
        loadMouse();

        randomizeCheese();
        Grid origin = maze.getGrid(0, 0);
        for (MouseController mouse : mouseList) {
            try {
                adapter.newMouse(mouse);
                mouse.setTargetGrid(origin);
            } catch (Exception ex) {
                Debug.out().println("Failed to create mouse represent");
                ex.printStackTrace();
            }
        }

        adapter.start();
    }

    /**
     * All instances of Mouse will report its location on the maze.
     * @param represent The MouseRepresent that is representing an instance of the Mouse on the game interface.
     * @param xOnField The actual X location of the mouse on the actual Maze (UI).
     * @param yOnField The actual Y location of the mouse on the actual Maze (UI).
     * @return The target grid if the mouse reaches it. Null if otherwise.
     */
    public Grid report(MouseRepresent represent, int xOnField, int yOnField) {
        try {
            MouseController mouse = represent.getMouseController();
            Grid targetGrid = mouse.getTargetGrid();
            int targetXOnGrid = getGridLeft(targetGrid.getX());
            int targetYOnGrid = getGridTop(targetGrid.getY());

            int leeway = GameConfig.PIXELS_ON_TARGET_LEEWAY;

            if ((xOnField >= targetXOnGrid - leeway && xOnField <= targetXOnGrid + leeway) &&
                    (yOnField >= targetYOnGrid - leeway && yOnField <= targetYOnGrid + leeway)) {
                Bomb detonatedBomb = null;
                for (Bomb bomb : bombList) {
                    if (bomb.getX() == targetGrid.getX() && bomb.getY() == targetGrid.getY() && !bomb.hasDetonated()) {
                        if (bomb.getMouse() != mouse.getMouse()) {
                            detonatedBomb = bomb;
                            bomb.detonate();
                            break;
                        }
                    }
                }

                if (detonatedBomb != null) {
                    this.adapter.detonateBomb(detonatedBomb);
                    Grid newGrid = getRandomGrid();
                    mouse.setTargetGrid(newGrid);

                    this.adapter.repositionMouse(mouse, newGrid);
                    mouse.getMouse().respawned();
                    return null;
                }

                if (this.cheese.getX() == targetGrid.getX() && this.cheese.getY() == targetGrid.getY()) {
                    represent.displayCheeseNumber();
                    mouse.increaseNumberOfCheese();
                    this.numberOfCheeseTaken++;

                    if (this.numberOfCheeseTaken == this.numberOfCheese) {
                        this.adapter.stop();
                    } else {
                        randomizeCheese();
                    }
                }

                return targetGrid;

            }

        } catch (Exception ex) {

        }

        return null;
    }

    /**
     *	Get the next move made by the mouse implementation.
     *    @param mouse The Mouse in question to get its next move.
     *    @param targetGrid The grid the Mouse is currently at
     *    @return The Mouse's next move.
     */
    public int getMouseNextMove(MouseController mouse, Grid targetGrid) {
        return mouse.onGrid(targetGrid, cheese);
    }

    /**
     *	Causes the Mouse to move based on its move decision
     *    @param mouse The Mouse in question to move
     *    @param move The move that Mouse is going to take
     */
    public void causeMouseMove(MouseController mouse, int move) {
        try {
            Grid targetGrid = mouse.getTargetGrid();
            Grid nextGrid = null;

            switch (move) {
                case Mouse.UP:
                    nextGrid = targetGrid.up();
                    break;

                case Mouse.DOWN:
                    nextGrid = targetGrid.down();
                    break;

                case Mouse.LEFT:
                    nextGrid = targetGrid.left();
                    break;

                case Mouse.RIGHT:
                    nextGrid = targetGrid.right();
                    break;

                case Mouse.BOMB:
                    nextGrid = null;

                    boolean canPlant = true;
                    for (Bomb bomb : bombList) {
                        if (!bomb.hasDetonated()) {
                            if (targetGrid.getX() == bomb.getX() && targetGrid.getY() == bomb.getY()) {
                                canPlant = false;
                            }
                        }
                    }

                    if (canPlant) {
                        Bomb bomb = mouse.makeBomb();

                        if (bomb != null) {
                            bombList.add(bomb);
                            this.adapter.newBomb(bomb);
                        }
                    }

                    break;
            }

            if (nextGrid != null) {
                mouse.setTargetGrid(nextGrid);
            }

        } catch (Exception ex) {
            Debug.out().println(mouse.getMouse().getName() + " encountered an exception.");
            Debug.out().println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Gets a random grid from the maze.
    private Grid getRandomGrid() {
        Random random = new Random();
        Grid grid = maze.getGrid(random.nextInt(maze.getWidth()), random.nextInt(maze.getHeight()));

        return grid;
    }

    // Calls the MouseLoader class to discover all Mouse implementations that are
    // playing on the field. These Mouse implementations are then loaded and wrapped
    // around by the MouseController class.
    private void loadMouse() {
        mouseList.clear();
        for (Class<?> clz : MouseLoader.load()) {
            try {
                Constructor<?> struc = clz.getConstructor((Class<?>[]) null);
                Mouse mouse = (Mouse) struc.newInstance();

                MouseController controller = new MouseController(mouse);
                controller.setNumberOfBombs((int) (this.numberOfCheese * GameConfig.RATIO_BOMBS_TO_CHEESE));

                mouseList.add(controller);
            } catch (Exception ex) {
                Debug.out().println(clz.getName() + " failed to load.");
                ex.printStackTrace();
            }
        }
    }


    // Change the location of the cheese randomly. Calls the adapter of the
    // new location of the cheese.
    private void randomizeCheese() {
        Random random = new Random();
        this.cheese = new Cheese(random.nextInt(maze.getWidth()), random.nextInt(maze.getHeight()));
        for (MouseController controller : mouseList) {
            controller.getMouse().newCheese();
        }
        adapter.newCheese(this.cheese);
    }


    /**
     * Gets the actual X-axis on the game interface given the maze X-axis value.
     * @param x The X-axis value on the maze.
     * @return The X-axis value on the game interface
     */
    public int getGridLeft(int x) {
        return x * gridSize;
    }

    /**
     * Gets the actual Y-axis on the game interface given the maze Y-axis value.
     * @param y The Y-axis value on the maze.
     * @return The Y-axis value on the game interface.
     */
    public int getGridTop(int y) {
        return (maze.getHeight() - y - 1) * gridSize;
    }

}