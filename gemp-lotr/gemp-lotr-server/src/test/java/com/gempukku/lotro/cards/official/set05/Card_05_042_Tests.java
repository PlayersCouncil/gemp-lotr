package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_05_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("turgon", "5_42");
					// Knight of Gondor: [gondor] Man, knight; a fellowship ability, never used
					put("kog", "5_36");
					// Pippin, Trollslayer: a [gondor] Hobbit, and a knight who is no Man; his skirmish ability is never used
					put("pippin", "103_31");
					// Ranger of Osgiliath: [gondor] Man who is no knight; a regroup ability, never used
					put("ranger", "7_116");

					// City Wall: [gondor] fortification; its Skirmish transfer is never used
					put("wall", "5_33");
					put("wall2", "5_33");
					// First Level: [gondor] fortification of another title; its Maneuver transfer works only from the
					// support area, and here it is on a minion
					put("level", "7_92");
					// Disarmed: a [gondor] condition that is no fortification; cheated onto a minion, so its
					// "when transferred" text never fires
					put("disarmed", "18_46");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, vitality 1, no text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TurgonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Turgon, Man of Belfalas
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Knight</b>. To play, spot a knight.
		 * <b>Maneuver:</b> Exert Turgon to wound a minion bearing a [gondor] fortification.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("turgon");

		assertEquals("Turgon", card.getBlueprint().getTitle());
		assertEquals("Man of Belfalas", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.KNIGHT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAKnightIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var turgon = scn.GetFreepsCard("turgon");
		var kog = scn.GetFreepsCard("kog");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToHand(turgon, kog);

		scn.StartGame();
		assertInHand(turgon, kog);
		assertInZone(Zone.FREE_CHARACTERS, ranger);

		// A [gondor] Man who is no knight, and Frodo: no knight to spot.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(turgon));

		scn.FreepsPlayCard(kog);
		assertInZone(Zone.FREE_CHARACTERS, kog);
		assertTrue(scn.FreepsPlayAvailable(turgon));

		scn.FreepsPlayCard(turgon);
		assertInZone(Zone.FREE_CHARACTERS, turgon);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AKnightWhoIsNoManLetsHimBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var turgon = scn.GetFreepsCard("turgon");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(turgon);

		scn.StartGame();
		assertInHand(turgon);
		assertInZone(Zone.FREE_CHARACTERS, pippin);

		assertTrue(scn.FreepsPlayAvailable(turgon));
		scn.FreepsPlayCard(turgon);
		assertInZone(Zone.FREE_CHARACTERS, turgon);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsToWoundAMinionBearingAGondorFortificationUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var turgon = scn.GetFreepsCard("turgon");
		var wall = scn.GetFreepsCard("wall");
		var wall2 = scn.GetFreepsCard("wall2");
		var level = scn.GetFreepsCard("level");
		var disarmed = scn.GetFreepsCard("disarmed");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(turgon);
		scn.MoveCardsToSupportArea(wall2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, brigand, troop);
		scn.AttachCardsTo(savage, level);
		scn.AttachCardsTo(brigand, wall);
		scn.AttachCardsTo(troop, disarmed);
		assertAttachedTo(level, savage);
		assertAttachedTo(wall, brigand);
		assertAttachedTo(disarmed, troop);
		assertInZone(Zone.SUPPORT, wall2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(turgon));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsActionAvailable(turgon));

		scn.FreepsUseCardAction(turgon);
		assertEquals(1, scn.GetWoundsOn(turgon));
		// Minions of two cultures and races, each bearing a [gondor] fortification of its own title; not the
		// Troop, who bears a [gondor] condition that is no fortification while a City Wall stands unborne in the
		// support area.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(brigand);

		// Vitality 1: the wound kills the Brigand.
		assertInDiscard(brigand);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The Savage is the one valid target left, and is not asked for.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(turgon));
		scn.FreepsUseCardAction(turgon);
		assertEquals(2, scn.GetWoundsOn(turgon));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));

		// Vitality 3 with 2 wounds: Turgon cannot exert again, though the Savage still bears First Level.
		scn.ShadowPass();
		assertAttachedTo(level, savage);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(turgon));
	}

	@Test
	public void WithNoMinionBearingAGondorFortificationHeExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var turgon = scn.GetFreepsCard("turgon");
		var wall = scn.GetFreepsCard("wall");
		var disarmed = scn.GetFreepsCard("disarmed");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(turgon);
		scn.MoveCardsToSupportArea(wall);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, disarmed);
		assertAttachedTo(disarmed, troop);
		assertInZone(Zone.SUPPORT, wall);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(turgon));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsActionAvailable(turgon));

		scn.FreepsUseCardAction(turgon);
		assertEquals(1, scn.GetWoundsOn(turgon));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
