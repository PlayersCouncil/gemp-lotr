package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_033_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spectre1", "103_33");
					put("spectre2", "103_33");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used here
					put("knight", "5_35");        // Gondorian Knight: a companion to play from hand for (2), with no requirement and no text but "Knight."
					put("guard", "1_7");          // Dwarf Guard: a companion with no text once in play, to bear the Armor
					put("armor", "1_8");          // Dwarven Armor: a Free Peoples possession that changes no number

					put("sky", "103_97");         // Ominous Sky: a Shadow condition in the support area; its trigger and ability are for the Shadow phase
					put("troop", "1_177");        // Goblin Patrol Troop: a minion with no text
					put("axe", "1_190");          // Moria Axe: a Shadow possession a [moria] Orc bears
					// Desert Wind Whisper: "When you play this minion, remove (4) or hinder this minion." Twilight cost 5.
					put("whisper", "103_50");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TormentedSpectreStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Tormented Spectre
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Wraith
		 * Strength: 5
		 * Vitality: 2
		 * Resistance: 6
		 * Game Text: Enduring.
		 * 	To play, add a threat.
		 * 	This companion is strength +1 for each hindered card you can spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spectre1");

		assertEquals("Tormented Spectre", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedWithTheThreatsAtTheirLimitUntilACompanionMakesRoomAndThenCostsTwoAndAddsAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spectre = scn.GetFreepsCard("spectre1");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCardsToHand(spectre, knight);
		// Frodo alone, and one threat: no more can be added. The Spectre is not yet a companion while its cost
		// is paid, so it makes no room for itself.
		scn.AddThreats(1);

		scn.StartGame();

		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetThreatLimit());
		assertFalse(scn.FreepsPlayAvailable(spectre));

		scn.FreepsPlayCard(knight);

		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(spectre));

		scn.FreepsPlayCard(spectre);

		// 1 + 1
		assertEquals(2, scn.GetThreats());
		// 2 for the Knight + 2 for the Spectre
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, spectre);
		// Nothing is hindered.
		assertEquals(5, scn.GetStrength(spectre));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsStrongerByOneFromAMinionHinderingItselfWhenPlayedUntilThatMinionIsRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spectre = scn.GetFreepsCard("spectre1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var whisper = scn.GetShadowCard("whisper");
		scn.MoveCompanionsToTable(spectre, aragorn);
		scn.MoveCardsToHand(whisper);

		scn.StartGame();
		// Enough for the Whisper: 5 to play + 2 roaming at site 2.
		scn.SetTwilight(8);
		scn.FreepsPass();
		scn.ShadowPlayCard(whisper);

		// In play, and not yet hindered while the Shadow player chooses.
		assertInZone(Zone.SHADOW_CHARACTERS, whisper);
		assertFalse(scn.IsHindered(whisper));
		assertEquals(5, scn.GetStrength(spectre));

		scn.ShadowChoose("Hinder");

		assertTrue(scn.IsHindered(whisper));
		assertEquals(5 + 1, scn.GetStrength(spectre));
		// Another companion gets nothing.
		assertEquals(8, scn.GetStrength(aragorn));

		scn.SkipToMovementDecision();

		// The Shadow player has reconciled, which restores his hindered cards: the Whisper is still in play and
		// no longer counts.
		assertInZone(Zone.SHADOW_CHARACTERS, whisper);
		assertFalse(scn.IsHindered(whisper));
		assertEquals(5, scn.GetStrength(spectre));
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void CountsEveryHinderedCardOfEitherSideAndOfEveryKindAndEachCopyCountsThemForItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spectre1 = scn.GetFreepsCard("spectre1");
		var spectre2 = scn.GetFreepsCard("spectre2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var sky = scn.GetShadowCard("sky");
		var troop = scn.GetShadowCard("troop");
		var axe = scn.GetShadowCard("axe");
		// The other player's own Aragorn, in his own fellowship: not active on this turn, so it cannot be spotted.
		var theirAragorn = scn.GetShadowCard("aragorn");
		var ring = scn.GetRing();
		scn.MoveCompanionsToTable(spectre1, spectre2, aragorn, guard);
		scn.AttachCardsTo(guard, armor);
		scn.MoveCompanionsToTable(theirAragorn);
		scn.MoveCardsToSupportArea(sky);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, axe);
		// Exhausted, which is not hindered.
		scn.AddWoundsToChar(aragorn, 3);

		assertEquals(5, scn.GetStrength(spectre1));
		assertEquals(5, scn.GetStrength(spectre2));

		// A Free Peoples companion
		scn.HinderCard(aragorn);
		assertEquals(5 + 1, scn.GetStrength(spectre1));
		// One hindered card is one for each copy, not two for both.
		assertEquals(5 + 1, scn.GetStrength(spectre2));

		// A Free Peoples possession on a companion who is not hindered
		scn.HinderCard(armor);
		assertEquals(5 + 2, scn.GetStrength(spectre1));

		// A Shadow condition in the support area
		scn.HinderCard(sky);
		assertEquals(5 + 3, scn.GetStrength(spectre1));

		// A minion
		scn.HinderCard(troop);
		assertEquals(5 + 4, scn.GetStrength(spectre1));

		// A Shadow possession on that hindered minion
		scn.HinderCard(axe);
		assertEquals(5 + 5, scn.GetStrength(spectre1));

		// The One Ring, which has no side and is still "a hindered card"
		scn.HinderCard(ring);
		assertEquals(5 + 6, scn.GetStrength(spectre1));
		assertEquals(5 + 6, scn.GetStrength(spectre2));

		scn.HinderCard(theirAragorn);
		assertTrue(scn.IsHindered(theirAragorn));
		assertEquals(5 + 6, scn.GetStrength(spectre1));

		// The other copy is a hindered card like any other.
		scn.HinderCard(spectre2);
		assertEquals(5 + 7, scn.GetStrength(spectre1));

		scn.RestoreCard(spectre2);
		assertEquals(5 + 6, scn.GetStrength(spectre1));
		assertEquals(5 + 6, scn.GetStrength(spectre2));

		scn.RestoreCard(aragorn);
		assertFalse(scn.IsHindered(aragorn));
		assertEquals(5 + 5, scn.GetStrength(spectre1));
		assertEquals(5 + 5, scn.GetStrength(spectre2));
		// Neither Aragorn nor the Guard gets anything.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
