# cs56-games-client-server

This project is a server and client in java to play games like tic tac toe or chess over a network.

* [Archive link](https://foo.cs.ucsb.edu/cs56/issues/0000535/)
* [Mantis link](https://foo.cs.ucsb.edu/56mantis/view.php?id=535)

project history
===============
```
 N/A
```

## Basic Usage
For best results, download JavaServer.jar and run it via command line. 


## Changes to project Structure
As noted in issue 1, converted basic project structure into a nicer MVC model.  While working through the code (which was originally going to just change the ChessGame into MVC, it was noted that the entire program needed to be reworked.  There is too much communication between the View and the Model in all 3 games and the Lobby to only make the Chess game into MVC.

An Example of why the code cannot be converted into MVC cleanly is provided below.  Rather than struggle through refactoring the project, it was decided between Adam and Professor Conrad that it would be wiser to rebuild the entire project into the correct MVC model, and make other various improvements to the code.  The new project is located here: https://github.com/UCSB-CS56-Projects/cs56-games-client-server-v2

```
/** changes the location of the client, in order to generate a service panel associated with
     * that location to start interacting with the specified service
     * @param L the service id number
     */
    public void changeLocation(int L) {
        if(location == L)
            return;
        location = L;
        if(location == -1) {
            canvasRef = new OfflineViewPanel(JavaServer.IP_ADDR,JavaServer.PORT);
        } else {

            int serviceType = services.get(location);
            if(serviceType == 0)
                canvasRef = new LobbyViewPanel();
            else if(serviceType == 1)
                canvasRef = new TicTacToeViewPanel();
            else if(serviceType == 2)
                canvasRef = new GomokuViewPanel();
            else if(serviceType == 3)
                canvasRef = new ChessViewPanel();
        }

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    messages = new ArrayList<MessageModel>();
                    //updateMessages();
                    container.remove(canvas);
                    canvas = canvasRef;
                    container.add(BorderLayout.CENTER, canvas);
                    canvas.addMouseListener(canvas);
                    //frame.validate();
                    container.validate();
                }
            }
        );
    }
```

Further explaining the segment of code above.  This chunk of code creates a reference to whichever "Panel" or `view` we are working with in the JavaClient class.  It then uses this reference to a view to manipulate the view directly instead of letting some middleman service such as a `controller` handling .  In addition the `view` also has a reference inside of it the `model` directly, which is not allowed by MVC.  Rather the `view` should interact with the `controller` to get important information pertaning the `view` from the `model`.  An example of this is provided below:

```
public void handleMessage(String string) {
        System.out.println("handling as Chess: "+string);
        if(string.indexOf("INIT;") == 0) {
            game.init();
//            capt1 = new ArrayList<Character>();
//            capt2 = new ArrayList<Character>();
        } else if(string.indexOf("STATE[") == 0) {
            game.setState(string);
            check = (game.isInCheck(game.turn)?game.turn:0);
            selectX = selectY = -1;
        } else if(string.indexOf("MOVE[") == 0) {
            String[] data = string.substring(5).split("]");
            int pid = Integer.parseInt(data[0]);
            String[] coords = data[1].split(",");
            int X1 = Integer.parseInt(coords[0]);
            int Y1 = Integer.parseInt(coords[1]);
            int X2 = Integer.parseInt(coords[2]);
            int Y2 = Integer.parseInt(coords[3]);

//            if(Character.isLetter(game.grid[Y2][X2]))
//                if(game.turn == 1)
//                    capt1.add(game.grid[Y2][X2]);
//                else
//                    capt2.add(game.grid[Y2][X2]);

//            game.grid[Y2][X2] = game.grid[Y1][X1];
//            game.grid[Y1][X1] = '0';
            game.tryMove(X1,Y1,X2,Y2);
            game.turn = pid;
            
            ...
```

The `view` should be as dumb as it can be and should not have access to the `model` or in this case `game` (the model reference).  In addition handling network code in the `view` is not a wise decision either, and should be handled at the `controller` level.

In the new version of this project, everything will be broken into MVC, and future improvements will be noted that can improve performance, add new features, and make it all around a nicer example of good MVC code.
