package mg.tsioryjonathan.ece_cost.util;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public final class Sorting {

  // plain String order would put "Bétonnière" after "Sable"
  public static final Comparator<String> FRENCH = Collator.getInstance(Locale.FRENCH)::compare;

  private Sorting() {
  }
}
