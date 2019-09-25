import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.input.Button;
import femto.palette.GrungeShift;
import femto.font.TIC80;

import backgrounds.Yard;
import backgrounds.TreesOne;
import backgrounds.TreesTwo;
import backgrounds.Houses;

import entities.Enemy;

import Tor;

class Main extends State {

    HiRes16Color screen; // the screenmode we want to draw with
    Yard yard;
    TreesOne treesOne;
    TreesTwo treesTwo;
    Houses houses;
    
    float tOneX, tTwoX, housesX;
    boolean jump = false, dashing = false;
    Tor tor;
    Enemy enemy;

    float dxs, dys, exs;
    int timer, dashTime;

    // start the game using Main as the initial state
    // and TIC80 as the menu's font
    public static void main(String[] args){
        Game.run( TIC80.font(), new Main() );
    }
    
    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init(){
        screen = new HiRes16Color(GrungeShift.palette(), TIC80.font());

        yard = new Yard();
        treesOne = new TreesOne();
        treesTwo = new TreesTwo();
        houses = new Houses();
        
        tOneX = 300;
        tTwoX = 500;
        housesX = 220;
        
        tor = new Tor();
        tor.y = 100;
        tor.x = 10;
        tor.dash();
        
        enemy = new Enemy();
        enemy.y = 100;
        enemy.x = 200;
        enemy.idle();
        
        dashTime = 0;
        timer = 0;
    }
    
    // Might help in certain situations
    void shutdown(){
        screen = null;
    }
    
    // update is called by femto.Game every frame
    void update(){
        screen.clear(0);
        if(jump){
            dys += 0.3;
        }else{
            dys = 0;
        }
        if(tor.y >= 100){
            tor.y = 100;
            jump = false;
            tor.dash();
        }

        dxs = 0;
        if( Button.A.justPressed()  && !jump) {
            dys = -4;
            jump = true;
            tor.jump();
        }else if(Button.B.isPressed() && dashTime > 0){
            dashTime--;
            dashing = true;
        }
        
        if(!Button.B.isPressed()){
            dashing = false;
        }
        
        if(dashTime <= 0 ){
            dashing = false;
            dashTime = 500;
        }
        
       // if(Button.Down.isPressed()) dys = 1;
        if(Button.Right.isPressed()) dxs = 2;
        if(Button.Left.isPressed()) dxs = -2;

        
        timer++;
        
        tOneX -= 3.5;
        tTwoX -= 3.25;
        
        housesX -= .60;
        
        exs -= 2;
        
        if(exs < -34) exs = 220;
        
        if(tOneX < -220) tOneX = Math.random(280, 400);
        if(tTwoX < -220) tTwoX = Math.random(220, 350);
        if(housesX < -220) housesX = Math.random(220, 300);
        
        yard.draw(screen, 0, 0);
        
        houses.draw(screen, housesX, 0);
        treesOne.draw(screen, tOneX, 0);
        treesTwo.draw(screen, tTwoX, 0);
        
        
        tor.x += dxs;
        tor.y += dys;
        tor.draw(screen); // Animation is updated automatically
        
        if(dashing){
            tor.draw(screen, tor.x-1, tor.y);
            tor.draw(screen, tor.x-2, tor.y);
            tor.draw(screen, tor.x-3, tor.y);
            tor.x+=7;
        }
        
        enemy.x = exs;
        enemy.draw(screen);
        
        // Update the screen with everything that was drawn
        screen.flush();
    }
    
}
