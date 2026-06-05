package pikachu.logic;

import javax.swing.*;

public class PokemonNode extends JButton {
    private int row;
    private int col;
    private int imageID;

    // cho bt pokemon o hang may cot may va id hinh
    public PokemonNode(int row, int col, int imageID) {
        super();
        this.row = row;
        this.col = col;
        this.imageID = imageID;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getImageID() {
        return imageID;
    }
    
    public void setImageID(int imageID) {
        this.imageID = imageID;
    }
}