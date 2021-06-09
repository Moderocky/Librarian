package mx.kenzie.librarian.test;

import mx.kenzie.librarian.Library;
import mx.kenzie.librarian.SimpleLibrary;
import org.junit.Test;

public class SimpleLibraryTest {
    
    @Test
    public void simple() {
        final Library<Object> library = new SimpleLibrary();
        class Alice {
            public int a() {
                return 1;
            }
            
            public int b() {
                return 2;
            }
        }
        assert library.register(new Alice());
        final Alice alice = library.getResource(Alice.class);
        assert alice != null;
        assert alice.a() == 1;
        assert alice.b() == 2;
    }
    
    @Test
    public void simpleBlind() {
        final Library<Object> library = new SimpleLibrary();
        //region Stubs
        interface Bob {
            int a();
            
            int b();
        }

        class Alice {
            public int a() {
                return 1;
            }
            
            public int b() {
                return 2;
            }
        }
        //endregion
        assert library.register(new Alice());
        final Bob bob = library.lookFor(Bob.class);
        assert bob != null;
        assert bob.a() == 1;
        assert bob.b() == 2;
    }
    
    @Test
    public void doubleBlind() {
        final Library<Object> library = new SimpleLibrary();
        //region Stubs
        interface Bob {
            int a();
            
            int b();
            
            int c();
            
            int d();
        }

        class Alice {
            public int a() {
                return 1;
            }
            
            public int b() {
                return 2;
            }
        }

        class Jeremy {
            public int c() {
                return 5;
            }
            
            public int d() {
                return 6;
            }
        }
        //endregion
        assert library.register(new Alice());
        assert library.register(new Jeremy());
        final Bob bob = library.lookFor(Bob.class);
        assert bob != null;
        assert bob.a() == 1;
        assert bob.b() == 2;
        assert bob.c() == 5;
        assert bob.d() == 6;
    }
    
    @Test
    public void managing() {
        final Library<Object> library = new SimpleLibrary();
        //region Stubs
        class Alice {
        }
        class Bob {
        }
        class Sarah extends Alice {
        }
        //endregion
        
        library.register(new Alice());
        library.register(new Bob());
        library.register(new Sarah());
        
        assert library.getResource(Alice.class).getClass() == Alice.class;
        assert library.getAllResources(Alice.class).length == 2;
        assert library.getAllResources(Bob.class).length == 1;
        assert library.getAllResources(Sarah.class).length == 1;
    }
    
    @Test
    public void example() {
        final Library<Object> library = new SimpleLibrary();
        //region Stubs
        class Player {
            double balance;
        }
        interface Economy {
            double getBalance(Player player);
            
            void setBalance(Player player, double balance);
        }
    
        class Blob {
            public double getBalance(Player player) {
                return player.balance;
            }
        
            public void setBalance(Player player, double balance) {
                player.balance = balance;
            }
        }
        //endregion
        final Player player = new Player();
        assert library.register(new Blob());
        final Economy economy = library.lookFor(Economy.class);
        assert economy.getBalance(player) == 0;
        economy.setBalance(player, 66);
        assert economy.getBalance(player) == 66;
    }
    
}
