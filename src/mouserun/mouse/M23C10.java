package mouserun.mouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import mouserun.game.Mouse;
import mouserun.game.Grid;
import mouserun.game.Cheese;

/**
 * @author Javier Francisco Dibo Gomez
 * @author Cristian Ojeda del Moral
 */
public class M23C10 extends Mouse {
    private Grid ultimaCeldaVisitada;
    private int movAnterior;

    // (Cordenadas, celda)
    private HashMap<Pair<Integer, Integer>, Grid> celdasVisitadas;

    private Stack<Grid> pilaCeldasVisitadas;

    private Stack<Integer> pilaMoviemientosRealizados;

    // Para mostrar mensajes System.err.println();
    public M23C10() {
        super("JFDG");
        celdasVisitadas = new HashMap<>();
        pilaCeldasVisitadas = new Stack<>();
        pilaMoviemientosRealizados = new Stack<>();
    }

    public void movimientosDireccion(int posX, int posY, int direccion, Grid celdaActual, ArrayList<Integer> listaMoviemientosNuevos, ArrayList<Integer> listaMovimientosBuenos, ArrayList<Integer> listaMovimientosMalos) {
        Pair<Integer, Integer> coordeandas;
        coordeandas = new Pair<>(posX, posY);
        if (!celdasVisitadas.containsKey(coordeandas)) {
            listaMoviemientosNuevos.add(direccion);
        } else if (celdaEsDistinta(direccion, celdaActual)) {
            listaMovimientosBuenos.add(direccion);
        } else {
            listaMovimientosMalos.add(direccion);
        }
    }

    public void movimientosPosibles(Grid celdaActual, ArrayList<Integer> listaMoviemientosNuevos, ArrayList<Integer> listaMovimientosBuenos, ArrayList<Integer> listaMovimientosMalos) {

        int posX, posY, direccion;

        posX = celdaActual.getX();
        posY = celdaActual.getY();

        if (celdaActual.canGoUp()) {
            direccion = Mouse.UP;
            movimientosDireccion(posX, posY + 1, direccion, celdaActual, listaMoviemientosNuevos, listaMovimientosBuenos, listaMovimientosMalos);
        }
        if (celdaActual.canGoDown()) {
            direccion = Mouse.DOWN;
            movimientosDireccion(posX, posY - 1, direccion, celdaActual, listaMoviemientosNuevos, listaMovimientosBuenos, listaMovimientosMalos);
        }
        if (celdaActual.canGoLeft()) {
            direccion = Mouse.LEFT;
            movimientosDireccion(posX - 1, posY, direccion, celdaActual, listaMoviemientosNuevos, listaMovimientosBuenos, listaMovimientosMalos);
        }
        if (celdaActual.canGoRight()) {
            direccion = Mouse.RIGHT;
            movimientosDireccion(posX + 1, posY, direccion, celdaActual, listaMoviemientosNuevos, listaMovimientosBuenos, listaMovimientosMalos);
        }

        System.err.println("\nMov nuevos: " + listaMoviemientosNuevos.toString() + "\nMov malos: " +
                listaMovimientosMalos.toString() + "\nMov buenos: " + listaMovimientosBuenos.toString() + "\n\n");
    }

    @Override
    public int move(Grid celdaActual, Cheese cheese) {

        Random aleatorio;
        ArrayList<Integer> listaMovimientosBuenos, listaMovimientosMalos, listaMovimientosNuevos;
        int movimientoPosibleAleatorio, posX, posY;
        boolean hayMovimientosBuenos, hayMovimientosNuevos;
        Pair<Integer, Integer> coordenadas;

        listaMovimientosBuenos = new ArrayList<>();
        listaMovimientosMalos = new ArrayList<>();
        listaMovimientosNuevos = new ArrayList<>();
        aleatorio = new Random();

        posX = celdaActual.getX();
        posY = celdaActual.getY();
        coordenadas = new Pair<>(posX, posY);

        celdasVisitadas.put(coordenadas, celdaActual);

        movimientosPosibles(celdaActual, listaMovimientosNuevos, listaMovimientosBuenos, listaMovimientosMalos);

        hayMovimientosBuenos = !listaMovimientosBuenos.isEmpty();
        hayMovimientosNuevos = !listaMovimientosNuevos.isEmpty();

        if (hayMovimientosNuevos) {
            movimientoPosibleAleatorio = aleatorio.nextInt(listaMovimientosNuevos.size());
            ultimaCeldaVisitada = celdaActual;
            pilaCeldasVisitadas.push(celdaActual);
            return listaMovimientosNuevos.get(movimientoPosibleAleatorio);
        } else if (hayMovimientosBuenos) {
            movimientoPosibleAleatorio = aleatorio.nextInt(listaMovimientosBuenos.size());
            pilaCeldasVisitadas.push(celdaActual);
            ultimaCeldaVisitada = celdaActual;
            return listaMovimientosBuenos.get(movimientoPosibleAleatorio);
        } else {
            movimientoPosibleAleatorio = aleatorio.nextInt(listaMovimientosMalos.size());
            pilaCeldasVisitadas.push(celdaActual);
            ultimaCeldaVisitada = celdaActual;
            return listaMovimientosMalos.get(movimientoPosibleAleatorio);
        }



    }

    @Override
    public void newCheese() {

    }

    @Override
    public void respawned() {

    }

    /**
     * Verifica si la celda actual es distinta de la última celda visitada
     *
     * @param miDireccion dirección en la que se mueve el raton
     * @param celdaActual celda actual en la que se encuentra el raton
     * @return true si la celda actual es distinta de la última celda visitada, false en caso contrario
     */
    public boolean celdaEsDistinta(int miDireccion, Grid celdaActual) {

        int posXActual, posYActual, posXAnterior, posYAnterior;
        boolean celdaEsDistinta;

        if (ultimaCeldaVisitada == null) {
            return true;
        }

        posXActual = celdaActual.getX();
        posYActual = celdaActual.getY();

        posXAnterior = ultimaCeldaVisitada.getX();
        posYAnterior = ultimaCeldaVisitada.getY();

        switch (miDireccion) {
            // Si estoy mirando a:
            case UP -> posYActual += 1;
            case DOWN -> posYActual -= 1;
            case LEFT -> posXActual -= 1;
            case RIGHT -> posXActual += 1;
        }

        celdaEsDistinta = !(posXAnterior == posXActual && posYAnterior == posYActual);

        return celdaEsDistinta;
    }

    /**
     * Comprueba si la celda esta almacenada en mi lista de celdas visitadas
     *
     * @param celdaActual la celda actual del raton
     * @param miDireccion la dirección en la que está mirando el raton
     * @return true si la celda ya ha sido visitada, false en caso contrario
     */
    public boolean celdaYaVisitada(Grid celdaActual, int miDireccion) {

        int posXActual, posYActual;
        Pair<Integer, Integer> coordenadas;
        boolean casillaPreviamenteVisitada;

        posXActual = celdaActual.getX();
        posYActual = celdaActual.getY();

        switch (miDireccion) {
            // Si estoy mirando a:
            case UP -> posYActual += 1;
            case DOWN -> posYActual -= 1;
            case LEFT -> posXActual -= 1;
            case RIGHT -> posXActual += 1;
        }
        coordenadas = new Pair<>(posXActual, posYActual);

        casillaPreviamenteVisitada = celdasVisitadas.containsKey(coordenadas);

        return casillaPreviamenteVisitada;
    }

    public boolean estoyMasArriba(Grid celdaActual, Grid celdaAnterior) {

        int posYActual, posYAnterior;
        boolean estoyMasArriba;

        posYActual = celdaActual.getY();
        posYAnterior = celdaAnterior.getY();

        estoyMasArriba = posYActual > posYAnterior;

        return estoyMasArriba;
    }

    public boolean estoyMasAbajo(Grid celdaActual, Grid celdaAnterior) {

        int posYActual, posYAnterior;
        boolean estoyMasAbajo;

        posYActual = celdaActual.getY();
        posYAnterior = celdaAnterior.getY();

        estoyMasAbajo = posYActual < posYAnterior;

        return estoyMasAbajo;
    }

    public boolean estoyMasDerecha(Grid celdaActual, Grid celdaAnterior) {

        int posXActual, posXAnterior;
        boolean estoyMasDerecha;

        posXActual = celdaActual.getX();
        posXAnterior = celdaAnterior.getX();

        estoyMasDerecha = posXActual > posXAnterior;

        return estoyMasDerecha;
    }

    public boolean estoyMasIzquierda(Grid celdaActual, Grid celdaAnterior) {

        int posXActual, posXAnterior;
        boolean estoyMasIzquierda;

        posXActual = celdaActual.getX();
        posXAnterior = celdaAnterior.getX();

        estoyMasIzquierda = posXActual < posXAnterior;

        return estoyMasIzquierda;
    }


    // Pair class
    class Pair<U, V> {

        public final U first;       // el primer campo de un par
        public final V second;      // el segundo campo de un par

        // Construye un nuevo par con valores especificados
        private Pair(U first, V second) {
            this.first = first;
            this.second = second;
        }

        @Override
        // Verifica que el objeto especificado sea "igual a" el objeto actual o no
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Pair<?, ?> pair = (Pair<?, ?>) o;

            // llamar al método `equals()` de los objetos subyacentes
            if (!first.equals(pair.first)) {
                return false;
            }
            return second.equals(pair.second);
        }

        @Override
        // Calcula el código hash de un objeto para admitir tablas hash
        public int hashCode() {
            // usa códigos hash de los objetos subyacentes
            return 31 * first.hashCode() + second.hashCode();
        }

        @Override
        public String toString() {
            return "(" + first + ", " + second + ")";
        }

    }

} // class MXXA04A
