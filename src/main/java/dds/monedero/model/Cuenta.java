package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {

  private double saldo = 0;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    saldo = 0;
  }

  public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public void poner(double cuanto) {

    if (isMontoNegativo(cuanto)) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }

    if (getMovimientosDelDia() >= 3) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }

    new Deposito(LocalDate.now(), cuanto).agregateA(this);
  }



  public void sacar(double cuanto) {

    if (isMontoNegativo(cuanto)) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }

    if (saldoEsMenorQue(cuanto)) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }

    validarExtraccion(cuanto);

    new Extraccion(LocalDate.now(), cuanto).agregateA(this);
  }

  public void validarExtraccion(double cuanto) {

    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    double limite = 1000 - montoExtraidoHoy;

    if (llegoAlMaxDeExtraccion(cuanto, limite)) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
          + " diarios, límite: " + limite);
    }
  }
  public long getMovimientosDelDia() {
    return getMovimientos().stream().filter(movimiento -> movimiento.fueDepositado(LocalDate.now())).count();
  }
  public boolean llegoAlMaxDeExtraccion(double cuanto, double limite) {
    return cuanto > limite;
  }
  public boolean saldoEsMenorQue(double cuanto) {
    return getSaldo() - cuanto < 0;
  }
  public boolean isMontoNegativo(double cuanto) {
    return cuanto <= 0;
  }

  public void agregarMovimiento(Movimiento movimiento) {
    movimientos.add(movimiento);
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> movimiento.fueExtraido(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

}
