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
    // (Cordenadas, celda)
    private final HashMap<Pair<Integer, Integer>, Grid> celdasVisitadas;
    private final Stack<Integer> pilaMovimientos;

    // Para mostrar mensajes System.err.println();
    public M23C10() {
        super("JFDG");
        celdasVisitadas = new HashMap<>();
        pilaMovimientos = new Stack<>();
    }

    /**
     * Este método registra el movimiento actual del ratón en las coordenadas dadas en la lista de movimientos,
     * si es que estas coordenadas aún no han sido visitadas. Este método utiliza un objeto Pair para almacenar
     * las coordenadas en un par ordenado y un Map para realizar una búsqueda eficiente de coordenadas visitadas.
     *
     * @param posX             la coordenada x del movimiento actual del ratón.
     * @param posY             la coordenada y del movimiento actual del ratón.
     * @param direccion        la dirección del movimiento actual del ratón.
     * @param listaMovimientos la lista de movimientos en la que se registrará el movimiento actual si es que estas coordenadas aún no han sido visitadas.
     */
    public void registrarCamino(int posX, int posY, int direccion, ArrayList<Integer> listaMovimientos) {
        Pair<Integer, Integer> coordeandas;
        coordeandas = new Pair<>(posX, posY);
        if (!celdasVisitadas.containsKey(coordeandas)) {
            listaMovimientos.add(direccion);
        }
    }

    /**
     * Este método calcula los movimientos posibles que el ratón puede hacer en la celda actual en el laberinto.
     * Se basa en las celdas vecinas de la celda actual y llama al método registrarCamino() para agregar los movimientos
     * posibles a la lista de movimientos pasada como parámetro.
     *
     * @param celdaActual      la celda actual en la que se encuentra el ratón.
     * @param listaMovimientos la lista de movimientos posibles a la que se agregarán los movimientos calculados.
     */
    public void movimientosPosibles(Grid celdaActual, ArrayList<Integer> listaMovimientos) {

        int posX, posY, direccion;

        posX = celdaActual.getX();
        posY = celdaActual.getY();

        if (celdaActual.canGoUp()) {
            direccion = Mouse.UP;
            registrarCamino(posX, posY + 1, direccion, listaMovimientos);
        }
        if (celdaActual.canGoDown()) {
            direccion = Mouse.DOWN;
            registrarCamino(posX, posY - 1, direccion, listaMovimientos);
        }
        if (celdaActual.canGoLeft()) {
            direccion = Mouse.LEFT;
            registrarCamino(posX - 1, posY, direccion, listaMovimientos);
        }
        if (celdaActual.canGoRight()) {
            direccion = Mouse.RIGHT;
            registrarCamino(posX + 1, posY, direccion, listaMovimientos);
        }
    }

    /**
     * Este método decide el siguiente movimiento del ratón en el laberinto. Si hay movimientos nuevos disponibles,
     * elige uno al azar y lo agrega a una pila de movimientos. Si no hay movimientos nuevos disponibles y la pila de
     * movimientos no está vacía, retrocede al último movimiento en la pila. Si no hay movimientos nuevos disponibles y
     * la pila de movimientos está vacía, elige un movimiento al azar.
     *
     * @param hayMovimientosNuevos un indicador booleano que indica si hay movimientos nuevos disponibles.
     * @param listaMovimientos     la lista de movimientos posibles a partir de la celda actual.
     * @return el siguiente movimiento del ratón a realizar.
     */
    public int decidirMovimiento(boolean hayMovimientosNuevos, ArrayList<Integer> listaMovimientos) {

        int movimientoPosibleAleatorio, movimientoFinal, ultimoMovimiento;
        Random aleatorio;

        aleatorio = new Random();
        movimientoFinal = 0;

        if (hayMovimientosNuevos) {
            movimientoPosibleAleatorio = aleatorio.nextInt(listaMovimientos.size());
            movimientoFinal = listaMovimientos.get(movimientoPosibleAleatorio);
            pilaMovimientos.push(movimientoFinal);
        } else if (!pilaMovimientos.isEmpty()) {
            ultimoMovimiento = pilaMovimientos.pop();
            switch (ultimoMovimiento) {
                case Mouse.UP -> movimientoFinal = Mouse.DOWN;
                case Mouse.DOWN -> movimientoFinal = Mouse.UP;
                case Mouse.RIGHT -> movimientoFinal = Mouse.LEFT;
                case Mouse.LEFT -> movimientoFinal = Mouse.RIGHT;
            }
        } else {
            movimientoFinal = aleatorio.nextInt(4) + 1;
        }

        return movimientoFinal;
    }

    /**
     * Este método mueve el ratón a la siguiente celda del laberinto. Primero registra la celda actual en un Map para
     * evitar visitas repetidas, luego genera una lista de movimientos posibles a partir de la celda actual y elige un
     * movimiento utilizando el método decidirMovimiento(). Finalmente, devuelve el movimiento elegido.
     *
     * @param celdaActual la celda actual en la que se encuentra el ratón.
     * @param cheese      el queso que el ratón está buscando.
     * @return el siguiente movimiento del ratón a realizar.
     */
    @Override
    public int move(Grid celdaActual, Cheese cheese) {

        ArrayList<Integer> listaMovimientos;
        int posX, posY, movimientoFinal;
        boolean hayMovimientosNuevos;
        Pair<Integer, Integer> coordenadas;

        listaMovimientos = new ArrayList<>();

        posX = celdaActual.getX();
        posY = celdaActual.getY();
        coordenadas = new Pair<>(posX, posY);

        celdasVisitadas.put(coordenadas, celdaActual);

        movimientosPosibles(celdaActual, listaMovimientos);

        hayMovimientosNuevos = !listaMovimientos.isEmpty();

        movimientoFinal = decidirMovimiento(hayMovimientosNuevos, listaMovimientos);

        // System.err.println("Num celdas exploradas=" + celdasVisitadas.size());

        return movimientoFinal;
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
