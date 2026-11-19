package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_031_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the skirmishes of
	// turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fight", "30_31");
					put("gandalf", "30_26");    // Gandalf, Friend of Thorin: the Gandalf to spot; a fellowship ability and
					                            // texts about [dwarven] followers and Thorin, none laid
					put("nori", "30_15");       // Nori, Of the Blue Mountains: a [dwarven] companion; a maneuver ability not used
					put("radagast", "31_17");   // Radagast, The Brown: a [gandalf] Wizard ally who is no Gandalf; a
					                            // fellowship ability not used
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; its text needs 3 Ents

					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6; an assignment ability not used
					put("bats", "32_30");       // Dark Bats: [moria] creature, strength 4; a Shadow ability not used
					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, strength 7; texts for site 9 and the regroup phase
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TakeuparmsFightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Take up arms. Fight!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Spell</b>.
		 * 	Spot Gandalf to make a minion strength -3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fight");

		assertEquals("Take up arms. Fight!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingGandalfMakesAnyMinionStrengthMinus3ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fight = scn.GetFreepsCard("fight");
		var gandalf = scn.GetFreepsCard("gandalf");
		var nori = scn.GetFreepsCard("nori");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, nori);
		scn.MoveCardsToHand(fight);

		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		var veteran = scn.GetShadowCard("veteran");

		scn.StartGame();
		assertInZone(Zone.HAND, fight);
		scn.MoveMinionsToTable(aggressor, bats, veteran);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, aggressor}, new PhysicalCardImpl[]{nori, bats});
		// The Orkish Veteran is left over, and stays unassigned.
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(6, scn.GetStrength(aggressor));
		assertEquals(4, scn.GetStrength(bats));
		assertEquals(7, scn.GetStrength(veteran));
		assertTrue(scn.FreepsPlayAvailable(fight));

		scn.FreepsPlayCard(fight);
		// Any minion: the one in this skirmish, one assigned to another companion, and one assigned to nobody, of
		// three cultures and two races. No companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, bats, veteran));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, nori, bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(veteran);

		assertInZone(Zone.DISCARD, fight);
		// 7 - 3
		assertEquals(4, scn.GetStrength(veteran));
		assertEquals(6, scn.GetStrength(aggressor));
		assertEquals(4, scn.GetStrength(bats));

		// Still in force as the skirmish draws to its end.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();
		assertEquals(4, scn.GetStrength(veteran));
		scn.FreepsPass();

		// Gandalf's skirmish is over; in the next, Nori's, the effect is gone.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(nori);
		assertEquals(7, scn.GetStrength(veteran));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutGandalfThoughOtherGandalfCardsAreInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fight = scn.GetFreepsCard("fight");
		var nori = scn.GetFreepsCard("nori");
		var radagast = scn.GetFreepsCard("radagast");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(nori, avenger);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToHand(fight);

		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		assertInZone(Zone.HAND, fight);
		scn.MoveMinionsToTable(aggressor);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(nori, aggressor);
		scn.FreepsResolveSkirmish(nori);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fight));
		assertInZone(Zone.HAND, fight);
	}
}
