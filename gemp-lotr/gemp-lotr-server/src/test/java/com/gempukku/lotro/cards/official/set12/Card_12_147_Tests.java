package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_147_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("suppressing", "12_147");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, resistance 6; a maneuver ability never used here
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, resistance 6; a skirmish ability never used here
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer, resistance 5
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit, strength 3; a skirmish ability never used here
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf; an archery ability never used here
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally; an archery ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo 1_290, the Ring-bearer: resistance 10
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SuppressingUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Suppressing Uruk
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 11
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While there is a companion in the dead pile, each unbound companion is resistance -4.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("suppressing");

		assertEquals("Suppressing Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void OnceItKillsACompanionEachUnboundCompanionIsResistanceMinus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var suppressing = scn.GetShadowCard("suppressing");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn, gimli, sam, merry);
		scn.MoveMinionsToTable(suppressing);

		scn.FreepsPass();
		scn.SkipToAssignments();
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));

		// Strength 11 against Merry's 3: more than double, so he is overwhelmed and killed.
		scn.FreepsAssignToMinions(merry, suppressing);
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, merry);
		assertEquals(1, scn.GetFreepsDeadCount());
		// The two unbound companions lose 4; the Ring-bearer and Sam, both Ring-bound, keep theirs.
		assertEquals(2, scn.GetResistance(aragorn));
		assertEquals(2, scn.GetResistance(gimli));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void AnAllyInTheDeadPileOrACompanionInTheShadowPlayersOwnDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var suppressing = scn.GetShadowCard("suppressing");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var orophin = scn.GetFreepsCard("orophin");
		var legolas = scn.GetFreepsCard("legolas");
		var shadowLegolas = scn.GetShadowCard("legolas");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveMinionsToTable(suppressing);
		// An ally in the dead pile, and a companion in the Shadow player's own dead pile: "the dead pile" is the
		// Free Peoples player's (Q-0131).
		scn.MoveCardsToDeadPile(orophin, shadowLegolas);
		assertInZone(Zone.DEAD, orophin, shadowLegolas);
		assertEquals(1, scn.GetFreepsDeadCount());

		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));

		// A companion of the Free Peoples player's own in his dead pile switches it on.
		scn.MoveCardsToDeadPile(legolas);
		assertEquals(2, scn.GetFreepsDeadCount());

		assertEquals(2, scn.GetResistance(aragorn));
		assertEquals(2, scn.GetResistance(gimli));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
