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
    int timer, dashTime, dashCharge, speed, torSpeed, torJump;
    
    
    int quantity, coffeeY;
    int[] coffees;

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
        tor.x = 16;
        tor.dash();
        
        enemy = new Enemy();
        enemy.y = 100;
        enemy.x = 200;
        enemy.idle();
        
        dashTime = 0;
        dashCharge = 50;
        timer = 0;
        speed = 0;
        torSpeed = 0;

        quantity = 0;
        int y = Math.random(70, 120);
        generateCoffeeDrops(y);
    }
    
    void generateCoffeeDrops( int y ){
        coffees = new int[6];
        int start = Math.random(220, 230);
        for(int i = 0; i < 6; i++){
            coffees[i] = 8*i+start;
        }
        coffeeY = y;
    }
    
    // Might help in certain situations
    void shutdown(){
        screen = null;
    }
    
    // update is called by femto.Game every frame
    void update(){
        screen.clear(0);
        
        timer++;
        
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

        if( Button.A.justPressed()  && !jump) {
            dys = -4+torJump;
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
            dashTime = 50;
        }

        //START Draw BG
        tOneX -= 3.5+speed;
        tTwoX -= 3.25+speed;
        housesX -= .60+speed;
        exs -= 2+speed;
        if(exs < -34) exs = 220;
        if(tOneX < -220) tOneX = Math.random(280, 400);
        if(tTwoX < -220) tTwoX = Math.random(220, 350);
        if(housesX < -220) housesX = Math.random(220, 300);
        yard.draw(screen, 0, 0);
        houses.draw(screen, housesX, 0);
        treesOne.draw(screen, tOneX, 0);
        treesTwo.draw(screen, tTwoX, 0);
        //END Draw BG
        if(dashing)speed = 2;
        else speed = 0;
        tor.y += dys;
        tor.draw(screen); // Animation is updated automatically
        
        if(dashing){
            tor.draw(screen, tor.x-2, tor.y);
            tor.draw(screen, tor.x-4, tor.y);
            tor.draw(screen, tor.x-6, tor.y);
            tor.x+=12;
        }
        
        checkCoffeeCollect();
        updateCoffeeDrops();
        drawCoffeeDrops();
        
        enemy.x = exs;
        enemy.draw(screen);
        
        
        drawUpgrades();
        
        // Update the screen with everything that was drawn
        screen.flush();
    }
    
    void checkCoffeeCollect(){
        for(int x = 0; x < 6; x++){
            if(tor.x < coffees[x] + 4 && tor.x + 24 > coffees[x] &&
                tor.y < coffeeY + 6 && tor.y + 24 > coffeeY)
            {
                System.out.println("Collision Detected: " + quantity);
                quantity++;
                coffees[x] = -20;
            }
        }
    }
    
    void updateCoffeeDrops(){
        int reset = 0;
        for(int x = 0; x < 6; x++){
            coffees[x]--;
            if(coffees[x] < 0 && coffees[x] > -10) coffees[x] = 221;
            if(coffees[x] < -10) reset++;
        }
        if(reset > 5){
            int y = Math.random(70, 120);
            generateCoffeeDrops(y);
        }
    }
    void drawCoffeeDrops(){
        for(int x : coffees){
            screen.fillRect(x, coffeeY, 4, 6, 10);
        }
    }
    
    void drawUpgrades(){
        for(int i = 0; i < 5; i++){
            if(quantity/10 == i){
                screen.fillRect(32*i+9, 151, 22, 8, 11);
            }
            screen.drawRect(32*i+8, 150, 24, 10, 10);
        }
        if(quantity > 50){
            screen.fillRect(32*4+9, 151, 22, 8, 11);
        }
    }
}
