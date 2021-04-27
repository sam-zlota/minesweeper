import java.util.ArrayList;

import java.util.Arrays;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;


import javalib.worldimages.*;

//interface for constants
interface Constants {
  int cellSize = 40;
}

//class to represent cell
class Cell implements Constants {
  boolean hidden;
  boolean isMine;
  boolean isFlagged;
  ArrayList<Cell> neighbors;

  /* TEMPLATE
   * fields:
   * this.hidden ... boolean
   * this.isMine ... boolean
   * this.isFlagged ... boolean
   * this.neighbors ... ArrayList<Cell>
   * methods:
   * this.drawCell() ... WorldImage
   * this.drawCellHelp() ... WorldImage
   * this.numMines() ... int
   */

  Cell(boolean hidden, boolean isMine, boolean isFlagged, ArrayList<Cell> neighbors) {
    this.hidden = hidden;
    this.isMine = isMine;
    this.isFlagged = isFlagged;
    this.neighbors = neighbors;     
  }

  Cell(boolean isMine) {
    this(true, isMine, false, null);
  }

  Cell(boolean isMine, ArrayList<Cell> neighbors) {
    this(true, isMine, false, neighbors);
  }

  //to draw the Cell
  public WorldImage drawCell() {
    Color bg = new Color(202, 213, 219);
    if (!this.hidden) {
      bg = new Color(79, 109, 142);
    }
    return new OverlayImage(this.drawCellHelp(), 
        new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, bg));
  }

  //helper for drawCell that creates foreground
  public WorldImage drawCellHelp()   {
    if (this.hidden) {
      if (this.isMine && this.isFlagged) {
        return new OverlayImage(
            new RotateImage(
                new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.ORANGE), 90.0),
            new RectangleImage(cellSize, cellSize, OutlineMode.OUTLINE, Color.BLACK));
      }
      else {
        return new RectangleImage(cellSize, cellSize, OutlineMode.OUTLINE, Color.BLACK);
      }
    }
    else if (this.isMine) {
      return new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, Color.RED);
    }
    else {
      int n = this.numMines();
      if (n == 0) {
        return new RectangleImage(cellSize, cellSize, OutlineMode.OUTLINE, Color.BLACK);
      }
      Color nColor = Color.BLACK;
      if (n == 1) {
        nColor = Color.RED;
      }
      if (n == 2) {
        nColor = Color.CYAN;
      }
      if (n == 3) {
        nColor = Color.yellow;
      }
      if (n == 4) {
        nColor = Color.green;
      }
      if (n > 4) {
        nColor = Color.MAGENTA;
      }

      WorldImage numImage = new TextImage(String.valueOf(n), 25, FontStyle.BOLD, nColor);
      return new OverlayImage(numImage, 
          new RectangleImage(cellSize, cellSize, OutlineMode.OUTLINE, Color.BLACK));
    }
  }

  //returns the number of mines in this Cell neighbors list
  public int numMines() {
    int count = 0;
    for (Cell c : this.neighbors) {
      if (c.isMine) {
        count++;
      }
    }
    return count;
  }

}

//class tor represent a MineSweeper game
class Game extends World implements Constants {

  int width;
  int height;
  int numMines;
  ArrayList<ArrayList<Cell>> cellmap;
  Random rand;


  /* TEMPLATE
   * fields:
   * this.width ... int
   * this.height ... int
   * this.numMines ... int
   * this.cellmap ... ArrayList<ArrayList<Cell>> 
   * this.rand ... Random
   * methods:
   * this.initializeCellMap() ... void
   * this.addMines() ... void
   * this.createMines(ArrayList<Posn>) ... void
   * this.isUnique(Posn, ArrayList<Posn>) ... boolean
   * this.updateNeighbors() ... void
   * this.getNeighbors(int, int) ... void
   * this.makeScene()... WorldScene
   * this.drawCellHelp() ... WorldImage
   * this.numMines() ... int
   */


  Game(int width, int height, int numMines, ArrayList<ArrayList<Cell>> cellmap, Random rand) {
    this.width = width;
    this.height = height;
    this.numMines = numMines;
    this.cellmap = cellmap;
    this.rand = rand;
  }

  Game(int width, int height, int numMines,
      ArrayList<ArrayList<Cell>> cellmap) {
    this(width, height, numMines, cellmap, new Random());
  }

  Game(int width, int height, int numMines) {
    this(width, height, numMines, null, new Random());
    this.initializeCellMap(width, height, numMines);
    this.addMines();
    this.updateNeighbors();
  } 

  // initializes the cellmap field in the Game class
  void initializeCellMap(int width, int height, int numMines) {
    ArrayList<ArrayList<Cell>> toBuild = new ArrayList<ArrayList<Cell>>();

    for (int row = 0; row < height; row++) {
      ArrayList<Cell> rowToBuild = new ArrayList<Cell>();

      for (int col = 0; col < width; col ++) {
        rowToBuild.add(new Cell(false));
      }
      toBuild.add(rowToBuild);
    }

    this.cellmap = toBuild;
  }

  // adds mines to the initialized Cells with randomness
  void addMines() {
    ArrayList<Posn> mineCoords = new ArrayList<Posn>(this.numMines);
    int mines = this.numMines;
    while (mines > 0) {
      int row = this.rand.nextInt(this.width);
      int col = this.rand.nextInt(this.height);
      Posn coord = new Posn(row, col);
      if (this.isUnique(coord, mineCoords)) {
        mineCoords.add(coord);
        mines--;
      }
    }
    this.createMines(mineCoords);

  }

  // Given coordinates as an ArrayList of Posns,
  // make the coorisponding cells in this cellmap have mines
  void createMines(ArrayList<Posn> mineCoords) {
    // TODO Auto-generated method stub
    for (Posn p: mineCoords) {
      cellmap.get(p.y).set(p.x, new Cell(true));
    }
  }


  // returns true if this posn is not in the given list of posn
  boolean isUnique(Posn coord, ArrayList<Posn> mineCoords) {

    for (Posn p: mineCoords) {
      if (p.x == coord.x && p.y == coord.y) {
        return false;
      }
    }
    return true;
  }

  //links all cells to their neighbors via the Cell field neighbors
  void updateNeighbors() {
    for (int row = 0; row < this.height; row++) {

      for (int col = 0; col < this.width; col++) {
        cellmap.get(row).get(col).neighbors = getNeighbors(col, row);
      }

    }
  }

  //gets the neighbors at of the cell at the given x and y index of the cellmap
  ArrayList<Cell> getNeighbors(int x, int y) {
    ArrayList<Cell> result = new ArrayList<Cell>();

    // test for above
    if (y - 1 >= 0 ) {
      result.add(this.cellmap.get(y - 1).get(x));
    }

    // test for below
    if (y + 1 < this.height ) {
      result.add(this.cellmap.get(y + 1).get(x));
    }

    // test for right
    if (x - 1 >= 0 ) {
      result.add(this.cellmap.get(y).get(x - 1));
    }

    // test for left
    if (x + 1 < this.width ) {
      result.add(this.cellmap.get(y).get(x + 1));
    }

    // test for upper right
    if (y - 1 >= 0 && x + 1 < this.width) {
      result.add(this.cellmap.get(y - 1).get(x + 1));
    }

    // test for upper left
    if (y - 1 >= 0 && x - 1 >= 0) {
      result.add(this.cellmap.get(y - 1).get(x - 1));
    }

    // test for bottom right
    if (y + 1 < this.height && x + 1 < this.width) {
      result.add(this.cellmap.get(y + 1).get(x + 1));
    }

    // test for bottom left
    if (y + 1 < this.height && x - 1 >= 0) {
      result.add(this.cellmap.get(y + 1).get(x - 1));
    }

    return result;

  }

  //returns a WorldScene that represents the current game state
  public WorldScene makeScene() {
    WorldScene scene = this.getEmptyScene();

    WorldImage toPlaceOnScene = new EmptyImage();
    for (ArrayList<Cell> row : this.cellmap) {
      WorldImage rowToDraw = new EmptyImage();

      for (Cell c : row) {
        rowToDraw = new BesideImage(rowToDraw, c.drawCell());
        // placing beside
      }
      toPlaceOnScene = new AboveImage(toPlaceOnScene, rowToDraw);

      // placing below
    }
    scene.placeImageXY(toPlaceOnScene, (this.width * cellSize) / 2 , (this.height * cellSize) / 2);
    return scene;

  }

}

//to test MineSweeper methods
class Examplesss implements Constants {

  ArrayList<ArrayList<Cell>> cm1; 

  Game g1;

  Game g2;

  ArrayList<Cell> cellCreateMines1 = new ArrayList<Cell>(Arrays.asList(
      new Cell(false), new Cell(false), new Cell(false)));
  ArrayList<Cell> cellCreateMines2 = new ArrayList<Cell>(Arrays.asList(
      new Cell(false), new Cell(true), new Cell(false)));
  ArrayList<Cell> cellCreateMines3 = new ArrayList<Cell>(Arrays.asList(
      new Cell(true), new Cell(false), new Cell(false)));

  ArrayList<ArrayList<Cell>> cellMapCreateMines = new ArrayList<ArrayList<Cell>>(
      Arrays.asList(cellCreateMines1,
          cellCreateMines2, cellCreateMines3));


  Cell cell00 = new Cell(false);
  Cell cell10 = new Cell(false);
  Cell cell20 = new Cell(false);
  Cell cell01 = new Cell(false);
  Cell cell11 = new Cell(false);
  Cell cell21 = new Cell(false);
  Cell cell02 = new Cell(false);
  Cell cell12 = new Cell(false);
  Cell cell22 = new Cell(false);


  ArrayList<Cell> cellUpdateNeighbors1 = new ArrayList<Cell>(Arrays.asList(cell00, cell10, cell20));
  ArrayList<Cell> cellUpdateNeighbors2 = new ArrayList<Cell>(Arrays.asList(cell01, cell11, cell21));
  ArrayList<Cell> cellUpdateNeighbors3 = new ArrayList<Cell>(Arrays.asList(cell02, cell12, cell22));

  ArrayList<ArrayList<Cell>> cellMapUpdateNeighbors = 
      new ArrayList<ArrayList<Cell>>(Arrays.asList(cellUpdateNeighbors1,
          cellUpdateNeighbors2, cellUpdateNeighbors3));

  Game g3 = new Game(3, 3, 0, cellMapUpdateNeighbors);

  ArrayList<Posn> posnIsUnique = new ArrayList<Posn>(Arrays.asList(new Posn(0, 0), new Posn(1, 0),
      new Posn(0, 3), new Posn(3, 2), new Posn(4, 6), new Posn(3, 1)));

  Game g4 = new Game(3, 3, 2);

  Game g5 = new Game(2,2,1);

  Game g6 = new Game(2,2,1);

  Cell hiddenCell = new Cell(true, false, false, new ArrayList<Cell>());
  Cell flaggedMine = new Cell(true, true, true,  new ArrayList<Cell>());
  Cell explodedMine = new Cell(false, true, false,  new ArrayList<Cell>());
  Cell noMineNeighbors = new Cell(false, false, false,  new ArrayList<Cell>());
  Cell withMineNeighbor = new Cell(false, false, false, 
      new ArrayList<Cell>(Arrays.asList(new Cell(true, true, false, null))));


  Cell forTesting1 = new Cell(false);
  Cell forTesting2 = new Cell(false);
  Cell forTesting3 = new Cell(false);
  Cell forTesting4 = new Cell(false);

  WorldImage hiddenCell2 = new RectangleImage(cellSize, cellSize, OutlineMode.OUTLINE, Color.BLACK);

  ArrayList<ArrayList<Cell>> cellMapForTesting = 
      new ArrayList<ArrayList<Cell>>(Arrays.asList(new ArrayList<Cell>(Arrays.asList(forTesting1, 
          forTesting2)), new ArrayList<Cell>(Arrays.asList(forTesting3, forTesting4))));

  WorldImage beside1 = new BesideImage(hiddenCell2, hiddenCell2);
  WorldImage above1 = new AboveImage(beside1, beside1);

  WorldScene mtScene = new Game(1,2,1).getEmptyScene();

  //initializes the fields of this class
  void initData() {
    g1 = new Game(3, 3, 3, null);
    ArrayList<Cell> c1 = new ArrayList<Cell>(Arrays.asList(
        new Cell(false), new Cell(false), new Cell(false)));
    ArrayList<Cell> c2 = new ArrayList<Cell>(Arrays.asList(
        new Cell(false), new Cell(false), new Cell(false)));
    ArrayList<Cell> c3 = new ArrayList<Cell>(Arrays.asList(
        new Cell(false), new Cell(false), new Cell(false)));
    cm1 = new ArrayList<ArrayList<Cell>>(Arrays.asList(c1, c2, c3));


    g2 = new Game(3, 3, 2, cm1);

  }

  //to run the game
  void testGo(Tester t) {
    Game myGame = new Game(15, 15, 50);

    myGame.bigBang(600, 600);
  }


  //tests the initializeCellMap method
  boolean testInitializeCellMap(Tester t) {
    this.initData();


    g1.initializeCellMap(g1.width, g1.height, g1.numMines);
    boolean finalConditions =
        t.checkExpect(g1.cellmap, cm1);

    return finalConditions;
  }

  //tests the addMines method
  void testAddMines(Tester t) {
    this.initData();

    Random r = new Random();
    r.setSeed(1);

    ArrayList<Cell> row1 = new ArrayList<Cell>(Arrays.asList(new Cell(false), new Cell(false)));
    ArrayList<Cell> row2 = new ArrayList<Cell>(Arrays.asList(new Cell(false), new Cell(false)));
    ArrayList<Cell> row3 = new ArrayList<Cell>(Arrays.asList(new Cell(false
        ), new Cell(false)));
    ArrayList<Cell> row4 = new ArrayList<Cell>(Arrays.asList(new Cell(false), new Cell(true)));

    ArrayList<ArrayList<Cell>> cm = new ArrayList<ArrayList<Cell>>(Arrays.asList(row1, row2));
    ArrayList<ArrayList<Cell>> cm2 = new ArrayList<ArrayList<Cell>>(Arrays.asList(row4, row3));

    Game gTest = new Game(2, 2, 1, cm, r);

    gTest.addMines();
    t.checkExpect(gTest, new Game(2, 2, 1, cm2));
  }

  //tests the createMines method
  boolean testCreateMines(Tester t) {
    this.initData();
    ArrayList<Posn> coords = new ArrayList<Posn>(Arrays.asList(new Posn(1, 1),
        new Posn(0, 2)));

    g2.createMines(coords);

    boolean finalConditions =
        t.checkExpect(g2.cellmap, cellMapCreateMines);

    return finalConditions;
  }

  //tests the isUnique method
  boolean testIsUnique(Tester t) {
    this.initData();

    return t.checkExpect(g1.isUnique(new Posn(0, 0), posnIsUnique), false) &&
        t.checkExpect(g1.isUnique(new Posn(50, 23), posnIsUnique), true);
  }

  //tests the updateNeighbors method
  void testUpdateNeighbors(Tester t) {
    this.initData();

    g3.updateNeighbors();

    t.checkExpect(g3.cellmap, cellMapUpdateNeighbors);

  }

  //tests the getNeighbors method
  boolean testGetNeighbors(Tester t) {
    this.initData();

    return t.checkExpect(g3.getNeighbors(1, 1), new ArrayList<Cell>(Arrays.asList(cell10, cell12,
        cell21, cell01, cell20, cell00, cell22, cell02))) && t.checkExpect(g3.getNeighbors(0, 0), 
            new ArrayList<Cell>(Arrays.asList(cell01, cell10, cell11)));

  }


  //tests the drawCellHelp method
  void testDrawCellHelp(Tester t) {
    t.checkExpect(hiddenCell.drawCellHelp(), new RectangleImage(cellSize, cellSize, 
        OutlineMode.OUTLINE, Color.BLACK));
    t.checkExpect(flaggedMine.drawCellHelp(), new OverlayImage(
        new RotateImage(
            new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.ORANGE), 90.0),
        new RectangleImage(cellSize, cellSize, OutlineMode.OUTLINE, Color.BLACK)));
    t.checkExpect(explodedMine.drawCellHelp(), new RectangleImage(cellSize, cellSize, 
        OutlineMode.SOLID, Color.RED));
    t.checkExpect(noMineNeighbors.drawCellHelp(), new RectangleImage(cellSize, cellSize, 
        OutlineMode.OUTLINE, Color.BLACK));
    t.checkExpect(withMineNeighbor.drawCellHelp(), new OverlayImage(new TextImage(String.valueOf(1),
        25, FontStyle.BOLD, Color.RED), 
        new RectangleImage(cellSize, cellSize, OutlineMode.OUTLINE, Color.BLACK)));
  }

  //tests the drawCell method
  void testDrawCell(Tester t) {
    t.checkExpect(hiddenCell.drawCell(), new OverlayImage(new RectangleImage(cellSize, cellSize,
        OutlineMode.OUTLINE, Color.BLACK),
        new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, new Color(202, 213, 219))));
    t.checkExpect(flaggedMine.drawCell(), new OverlayImage(new OverlayImage(
        new RotateImage(
            new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.ORANGE), 90.0),
        new RectangleImage(cellSize, cellSize, OutlineMode.OUTLINE, Color.BLACK)),
        new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, new Color(202, 213, 219))));
    t.checkExpect(explodedMine.drawCell(), new OverlayImage(new RectangleImage(cellSize, cellSize, 
        OutlineMode.SOLID, Color.RED),
        new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, new Color(79, 109, 142))));
    t.checkExpect(noMineNeighbors.drawCell(), new OverlayImage(new RectangleImage(cellSize, 
        cellSize, OutlineMode.OUTLINE, Color.BLACK),
        new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, new Color(79,109,142))));
    t.checkExpect(withMineNeighbor.drawCell(), new OverlayImage(new OverlayImage(new 
        TextImage(String.valueOf(1), 25, FontStyle.BOLD, Color.RED), 
        new RectangleImage(cellSize, cellSize, OutlineMode.OUTLINE, Color.BLACK)),
        new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, new Color(79,109,142))));
  }

  //tests the numMines method
  void testNumMines(Tester t) {
    t.checkExpect(noMineNeighbors.numMines(), 0);
    t.checkExpect(withMineNeighbor.numMines(), 1);
  }

  //tests the makeScene method
  void testMakeScene(Tester t) {
    WorldScene result = g6.getEmptyScene();

    WorldImage toPlaceOnScene = new EmptyImage();
    //WorldImage toPlaceOnScene2 = new EmptyImage();
    WorldImage rowToDraw1 = new EmptyImage();
    WorldImage rowToDraw2 = new EmptyImage();

    rowToDraw1 = new BesideImage(rowToDraw1, g6.cellmap.get(0).get(0).drawCell());
    rowToDraw1 = new BesideImage(rowToDraw1, g6.cellmap.get(0).get(1).drawCell());

    rowToDraw2 = new BesideImage(rowToDraw2, g6.cellmap.get(1).get(0).drawCell());
    rowToDraw2 = new BesideImage(rowToDraw2, g6.cellmap.get(1).get(1).drawCell());

    toPlaceOnScene = new AboveImage(toPlaceOnScene, rowToDraw1);
    toPlaceOnScene = new AboveImage(toPlaceOnScene, rowToDraw2);

    result.placeImageXY(toPlaceOnScene, (g6.width * cellSize) / 2, (g6.height * cellSize) / 2);

    t.checkExpect(g5.makeScene(), result);
  }

}
