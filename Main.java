import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.input.Button;
import femto.palette.GrungeShift;
import femto.font.TIC80;


import Tor;

class Main extends State {

    HiRes16Color screen; // the screenmode we want to draw with


    Tor tor;

    Pattern background; // static image
    float angle; // floats are actually FixedPoint (23.8)
    int dxs, dys;
    int counter, speed; // variables are automatically initialized to 0 or null

    // start the game using Main as the initial state
    // and TIC80 as the menu's font
    public static void main(String[] args){
        Game.run( TIC80.font(), new Main() );
    }
    
    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init(){
        screen = new HiRes16Color(GrungeShift.palette(), TIC80.font());
        background = new Pattern();
        speed = 0;
        
        tor = new Tor();
        tor.y = 70;
        tor.dash();
    }
    
    // Might help in certain situations
    void shutdown(){
        screen = null;
    }
    
    // update is called by femto.Game every frame
    void update(){
        screen.clear(0);
        dys = 0;
        dxs = 0;
        if(Button.Up.isPressed()) dys = -1;
        if(Button.Down.isPressed()) dys = 1;
        if(Button.Right.isPressed())dxs = 1;
        if(Button.Left.isPressed()) dxs = -1;
        
        // Change to a new state when A is pressed
        if( Button.A.justPressed() )
            Game.changeState( new Main() );

        // Fill the screen using Pattern.png
            for( int x=0; x<440; x += background.width() ){
                if(x+speed < -background.width()*2) speed = 0;
                background.draw(screen, x+speed, 100);
            }
        speed--;
        counter++;
        
        tor.x += dxs;
        tor.y += dys;
        tor.draw(screen); // Animation is updated automatically
        
        // Update the screen with everything that was drawn
        screen.flush();
    }
    
}
