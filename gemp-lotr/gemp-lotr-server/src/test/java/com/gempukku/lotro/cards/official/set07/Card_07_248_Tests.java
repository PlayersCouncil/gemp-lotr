package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_248_Tests
{

	// The Fellowship path, so that Edoras Hall 7_330 (each [rohan] possession costs 1 less) is not under the play.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("javelin1", "7_248");
					put("javelin2", "7_248");
					put("rider1", "4_286");     // Rider of Rohan: [rohan] Man companion, not unique; a skirmish ability used only on purpose
					put("rider2", "4_286");
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally; an archery ability that needs a villager
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit companion; a Man only beside 3 [rohan] companions or Eowyn
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimJavelinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Rohirrim Javelin
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Strength: +1
		 * Game Text: Bearer must be a [rohan] Man.
		 * 	<b>Archery:</b> Discard this possession to make the fellowship archery total +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("javelin1");

		assertEquals("Rohirrim Javelin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnARohanManCompanionOrAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var javelin = scn.GetFreepsCard("javelin1");
		var rider = scn.GetFreepsCard("rider1");
		var aldor = scn.GetFreepsCard("aldor");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(rider, boromir, merry);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(javelin);

		scn.StartGame();

		assertInHand(javelin);
		assertTrue(scn.FreepsPlayAvailable(javelin));
		scn.FreepsPlayCard(javelin);
		// A [rohan] Man companion and a [rohan] Man ally; not a [gondor] Man, nor Merry, a [rohan] Hobbit who is no
		// Man beside two [rohan] companions and no Eowyn, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, aldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(aldor);
		assertAttachedTo(javelin, aldor);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryAbilityDiscardsThisJavelinToMakeTheFellowshipArcheryTotalOneMoreForTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var javelin1 = scn.GetFreepsCard("javelin1");
		var javelin2 = scn.GetFreepsCard("javelin2");
		var rider1 = scn.GetFreepsCard("rider1");
		var rider2 = scn.GetFreepsCard("rider2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(rider1, rider2);
		// One ranged weapon to a bearer: a copy on each Rider.
		scn.AttachCardsTo(rider1, javelin1);
		scn.AttachCardsTo(rider2, javelin2);

		scn.StartGame();
		assertAttachedTo(javelin1, rider1);
		assertAttachedTo(javelin2, rider2);
		scn.MoveMinionsToTable(troop);

		// The phase before: not offered.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(javelin1));
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(javelin1));
		// Nobody in the fellowship is an archer.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.FreepsUseCardAction(javelin1);
		// The javelin used is the one discarded; the other copy stays on the second Rider and keeps its own ability.
		assertInDiscard(javelin1);
		assertAttachedTo(javelin2, rider2);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(javelin2));
		scn.FreepsPass();
		scn.ShadowPass();

		// The one archery wound goes to the only minion; the +1 lasted the archery phase and no longer.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertFalse(scn.FreepsActionAvailable(javelin2));
		assertAttachedTo(javelin2, rider2);
	}
}
