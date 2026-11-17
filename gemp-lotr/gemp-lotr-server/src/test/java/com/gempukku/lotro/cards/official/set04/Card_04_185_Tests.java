package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_185_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fanatic", "4_185");
					put("fanatic2", "4_185");
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9; Damage +1 is its whole text
					// Tower Assassin: [sauron] Orc, strength 11, vitality 3.  "Maneuver: Exert Tower Assassin to
					// wound an ally."
					put("assassin", "2_93");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] Dwarf companion
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an [elven] Elf ally; a fellowship ability only
					// Rosie Cotton, Hobbiton Lass: [shire] Hobbit ally, vitality 2, home 1.  Her texts are about Sam.
					put("rosie", "1_309");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukFanaticStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Fanatic
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * If there are at least 3 cards in the dead pile, this minion is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fanatic");

		assertEquals("Uruk Fanatic", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsFierceWithThreeCardsInTheFreePeoplesDeadPileAndNotWithTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fanatic = scn.GetShadowCard("fanatic");
		var fanatic2 = scn.GetShadowCard("fanatic2");
		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var celeborn = scn.GetFreepsCard("celeborn");
		var rosie = scn.GetFreepsCard("rosie");
		// The Shadow player's own ally, killed on one of his own turns.
		var shadowRosie = scn.GetShadowCard("rosie");

		scn.StartGame();
		scn.MoveMinionsToTable(fanatic, fanatic2, guard);
		// Two cards in the Free Peoples dead pile, a companion and an ally; a companion in his discard pile; and a
		// card in the Shadow player's own dead pile, which is not "the dead pile" (Q-0131).
		scn.MoveCardsToDeadPile(aragorn, celeborn);
		scn.MoveCardsToDiscard(gimli);
		scn.MoveCardsToDeadPile(shadowRosie);
		assertEquals(2, scn.GetFreepsDeadCount());
		assertInZone(Zone.DISCARD, gimli);
		assertInZone(Zone.DEAD, shadowRosie);
		assertFalse(scn.HasKeyword(fanatic, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(fanatic2, Keyword.FIERCE));

		scn.MoveCardsToDeadPile(rosie);
		assertEquals(3, scn.GetFreepsDeadCount());
		assertTrue(scn.HasKeyword(fanatic, Keyword.FIERCE));
		// Each copy for itself; another Uruk-hai is not "this minion".
		assertTrue(scn.HasKeyword(fanatic2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(guard, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AThirdCharacterKilledMakesTheFanaticFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fanatic = scn.GetShadowCard("fanatic");
		var assassin = scn.GetShadowCard("assassin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveCardsToSupportArea(rosie);

		scn.StartGame();
		scn.MoveMinionsToTable(fanatic, assassin);
		scn.MoveCardsToDeadPile(aragorn, celeborn);
		// Vitality 2 with 1 wound: the next wound kills her.
		scn.AddWoundsToChar(rosie, 1);
		assertEquals(1, scn.GetWoundsOn(rosie));
		assertInZone(Zone.SUPPORT, rosie);
		assertEquals(2, scn.GetFreepsDeadCount());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertFalse(scn.HasKeyword(fanatic, Keyword.FIERCE));

		assertEquals(0, scn.GetWoundsOn(assassin));
		scn.ShadowUseCardAction(assassin);
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertInZone(Zone.DEAD, rosie);
		assertEquals(3, scn.GetFreepsDeadCount());
		assertTrue(scn.HasKeyword(fanatic, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
