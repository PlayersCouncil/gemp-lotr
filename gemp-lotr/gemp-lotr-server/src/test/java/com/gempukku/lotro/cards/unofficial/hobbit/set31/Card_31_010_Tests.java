package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_010_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The tests are in
	// the maneuver phase at The Troll Hoard 30_50 (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tauriel", "31_10");
					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, strength 7; archer only at site 9, a Regroup ability not used
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6; an Assignment ability, not used
					put("footman", "30_42");    // Goblin Footman: [moria] Orc, strength 8; a "when you play" trigger only
					put("bats", "32_30");       // Dark Bats: [moria] Creature, strength 4, a minion that is no Orc; a Shadow ability, not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TaurielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Tauriel, Staunch Defender
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Home: 5H
		 * Strength: 6
		 * Vitality: 3
		 * Game Text: <b>Archer</b>.<br><b>Maneuver:</b> Exert Tauriel to return an Orc with strength 7 or less to its owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tauriel");

		assertEquals("Tauriel", card.getBlueprint().getTitle());
		assertEquals("Staunch Defender", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 5)));
	}

	@Test
	public void ManeuverAbilityExertsTaurielToReturnAnOrcOfStrength7OrLessToItsOwnersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tauriel = scn.GetFreepsCard("tauriel");
		var veteran = scn.GetShadowCard("veteran");
		var aggressor = scn.GetShadowCard("aggressor");
		var footman = scn.GetShadowCard("footman");
		var bats = scn.GetShadowCard("bats");
		scn.MoveAlliesToTable(tauriel);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, aggressor, footman, bats);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(7, scn.GetStrength(veteran));
		assertEquals(6, scn.GetStrength(aggressor));
		assertEquals(8, scn.GetStrength(footman));
		assertEquals(4, scn.GetStrength(bats));
		assertEquals(0, scn.GetWoundsOn(tauriel));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(tauriel));
		scn.FreepsUseCardAction(tauriel);

		assertEquals(1, scn.GetWoundsOn(tauriel));
		// Orcs of strength 7 and 6, of two cultures; not the Orc of strength 8, nor the minion that is no Orc.
		assertTrue(scn.FreepsHasCardChoiceAvailable(veteran, aggressor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(footman, bats));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(veteran);

		assertInZone(Zone.HAND, veteran);
		assertTrue(scn.GetShadowHand().contains(veteran));
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor, footman, bats);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoOrcOfStrength7OrLessTheAbilityIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tauriel = scn.GetFreepsCard("tauriel");
		var footman = scn.GetShadowCard("footman");
		var bats = scn.GetShadowCard("bats");
		scn.MoveAlliesToTable(tauriel);

		scn.StartGame();
		scn.MoveMinionsToTable(footman, bats);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(tauriel));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(tauriel));
		scn.FreepsUseCardAction(tauriel);

		assertEquals(1, scn.GetWoundsOn(tauriel));
		assertInZone(Zone.SHADOW_CHARACTERS, footman, bats);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTaurielIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tauriel = scn.GetFreepsCard("tauriel");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveAlliesToTable(tauriel);
		// Vitality 3 with 2 wounds: she cannot exert.
		scn.AddWoundsToChar(tauriel, 2);
		assertEquals(2, scn.GetWoundsOn(tauriel));

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(tauriel));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(tauriel));
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
	}
}
