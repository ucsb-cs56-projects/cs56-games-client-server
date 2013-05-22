package edu.ucsb.cs56.games.client_server.Models;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * stores resources for recycling
 * currently, just has chess pieces image, but will soon have more images and sounds for audio notification
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class ResModel {
    public static Image ChessPieces;

    public ResModel() {

    }

    public static void init(Class frame) {
        ChessPieces = null;
        try {
        	URL url = frame.getResource("graphics/pieces.png");
            ChessPieces = ImageIO.read(url);

        } catch (IOException e) {
            System.out.println("oops");
        }
    }
}