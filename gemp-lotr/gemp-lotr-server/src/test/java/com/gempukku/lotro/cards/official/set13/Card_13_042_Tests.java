package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "13_42");
					// Gandalf, The Grey Wizard: a Wizard companion; his Fellowship ability is used only on purpose
					put("gandalf", "1_364");
					// Radagast, The Brown: a Wizard ally (Hobbit set, Q-0312: the one Free Peoples Wizard who is no
					// companion); his Fellowship ability is used only on purpose
					put("radagast", "31_17");
					// Ent Avenger: a [gandalf] companion who is no Wizard; his text wants 3 Ents
					put("avenger", "6_27");
					// Treebeard, Keeper of the Watchwood: Regroup: exert him twice to play a [gandalf] condition from
					// the discard pile and make an opponent discard one of his conditions
					put("treebeard", "10_18");
					// Saruman, Keeper of Isengard: the Shadow player's Wizard; he may not be assigned to a skirmish,
					// so he is still there in the regroup phase
					put("saruman", "3_68");
					// Fear and Great Wonder: a [gandalf] condition that takes a [gandalf] token as it is played
					put("fear", "13_30");
					// Weary: an [isengard] condition the Shadow player plays on a companion
					put("weary", "4_212");
					// Hemmed In: a [men] condition in the Shadow support area; its ability needs a [men] minion
					put("hemmed", "12_70");
					// Spider Poison: a Shadow possession in the Shadow support area, a Shadow card that is no condition
					put("poison", "8_28");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TravelersHomesteadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Traveler's Homestead
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a Wizard.
		 * When you play this, add X [gandalf] tokens here, where X is the current region number.
		 * <b>Maneuver:</b> Discard this from play or remove 2 tokens from here to discard a Shadow condition from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("homestead");

		assertEquals("Traveler's Homestead", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAWizardIsInPlayAndInRegionOneAddsOneTokenHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var fear = scn.GetFreepsCard("fear");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToSupportArea(fear);
		scn.AddTokensToCard(fear, 1);
		scn.MoveCardsToHand(homestead, gandalf);

		scn.StartGame();

		// Ent Avenger is a [gandalf] companion, but no Wizard.
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertInZone(Zone.HAND, homestead);
		assertFalse(scn.FreepsPlayAvailable(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);

		assertTrue(scn.FreepsPlayAvailable(homestead));
		scn.FreepsPlayCard(homestead);
		assertInZone(Zone.SUPPORT, homestead);
		// Site 1 is in region 1.  The tokens go here, and not on the other [gandalf] card that has one.
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AWizardAllyAloneLetsItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToHand(homestead);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, radagast);
		assertTrue(scn.FreepsPlayAvailable(homestead));
		scn.FreepsPlayCard(homestead);
		assertInZone(Zone.SUPPORT, homestead);
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOpponentsSarumanLetsTreebeardPlayItFromTheDiscardPileInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var treebeard = scn.GetFreepsCard("treebeard");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCompanionsToTable(treebeard);
		scn.MoveCardsToDiscard(homestead);

		scn.StartGame();
		assertInDiscard(homestead);
		// Saruman comes down in the Shadow phase and, since he may not be assigned, is still there in the regroup
		// phase: the only Wizard on the table is the Shadow player's.
		scn.MoveMinionsToTable(saruman);
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertTrue(scn.FreepsActionAvailable(treebeard));
		scn.FreepsUseCardAction(treebeard);

		assertEquals(2, scn.GetWoundsOn(treebeard));
		assertInZone(Zone.SUPPORT, homestead);
		// Site 2 is in region 1.
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void PlayedInRegionTwoItAddsTwoTokensHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var gandalf = scn.GetFreepsCard("gandalf");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(homestead);
		scn.FreepsRefreshDecision();

		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsPlayCard(homestead);
		assertInZone(Zone.SUPPORT, homestead);
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedInRegionThreeItAddsThreeTokensHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var gandalf = scn.GetFreepsCard("gandalf");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(homestead);
		scn.FreepsRefreshDecision();

		// Region 3 is the last: no board reads more.
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.FreepsPlayCard(homestead);
		assertInZone(Zone.SUPPORT, homestead);
		assertEquals(3, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGandalfConditionPlayedAfterItAddsNoTokenHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fear = scn.GetFreepsCard("fear");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(homestead, fear);

		scn.StartGame();

		scn.FreepsPlayCard(homestead);
		assertEquals(1, scn.GetCultureTokensOn(homestead));

		scn.FreepsPlayCard(fear);
		assertInZone(Zone.SUPPORT, fear);
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RemovesTwoTokensToDiscardAShadowConditionBorneByACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var homestead = scn.GetFreepsCard("homestead");
		var fear = scn.GetFreepsCard("fear");
		scn.MoveCardsToSupportArea(homestead, fear);
		scn.AddTokensToCard(homestead, 3);
		scn.AddTokensToCard(fear, 1);

		var weary = scn.GetShadowCard("weary");
		var hemmed = scn.GetShadowCard("hemmed");
		var poison = scn.GetShadowCard("poison");
		scn.AttachCardsTo(frodo, weary);
		scn.MoveCardsToSupportArea(hemmed, poison);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, homestead, fear);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetCultureTokensOn(homestead));
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertAttachedTo(weary, frodo);
		assertInZone(Zone.SUPPORT, hemmed, poison);

		assertTrue(scn.FreepsActionAvailable(homestead));
		scn.FreepsUseCardAction(homestead);

		// With two tokens or more, either cost may be paid.
		assertEquals(List.of("Discard this from play", "Remove 2 tokens from here"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Remove 2 tokens from here");
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertInZone(Zone.SUPPORT, homestead);

		// A Shadow condition on a companion and one in the support area.  Not a Shadow possession, nor a Free
		// Peoples condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(weary, hemmed));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(poison, fear, homestead));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(weary);

		assertInDiscard(weary);
		assertFalse(scn.GetAttachedCards(frodo).contains(weary));
		assertInZone(Zone.SUPPORT, hemmed, poison, fear);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOneTokenHereItIsDiscardedToPayAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var fear = scn.GetFreepsCard("fear");
		scn.MoveCardsToSupportArea(homestead, fear);
		scn.AddTokensToCard(homestead, 1);
		// Two [gandalf] tokens elsewhere: they are not "here".
		scn.AddTokensToCard(fear, 2);

		var weary = scn.GetShadowCard("weary");
		var hemmed = scn.GetShadowCard("hemmed");
		scn.AttachCardsTo(scn.GetRingBearer(), weary);
		scn.MoveCardsToSupportArea(hemmed);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, homestead, fear);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertEquals(2, scn.GetCultureTokensOn(fear));
		assertAttachedTo(weary, scn.GetRingBearer());
		assertInZone(Zone.SUPPORT, hemmed);
		assertTrue(scn.FreepsActionAvailable(homestead));
		scn.FreepsUseCardAction(homestead);

		// Two tokens cannot be removed from here: the Homestead is discarded and the condition is chosen at once.
		assertInDiscard(homestead);
		assertEquals(2, scn.GetCultureTokensOn(fear));
		assertTrue(scn.FreepsHasCardChoiceAvailable(weary, hemmed));
		scn.FreepsChooseCard(hemmed);

		assertInDiscard(hemmed);
		assertAttachedTo(weary, scn.GetRingBearer());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoShadowConditionItIsStillUsedAndDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var fear = scn.GetFreepsCard("fear");
		scn.MoveCardsToSupportArea(homestead, fear);
		scn.AddTokensToCard(homestead, 3);

		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(poison);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, homestead);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, poison, fear);
		int shadowDiscard = scn.GetShadowDiscardCount();

		// The discard stands after "to": the cost is paid for nothing.
		assertTrue(scn.FreepsActionAvailable(homestead));
		scn.FreepsUseCardAction(homestead);
		scn.FreepsChoose("Remove 2 tokens from here");

		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, poison, fear, homestead);
		assertEquals(shadowDiscard, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		scn.MoveCardsToSupportArea(homestead);
		scn.AddTokensToCard(homestead, 3);

		var hemmed = scn.GetShadowCard("hemmed");
		scn.MoveCardsToSupportArea(hemmed);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(homestead));
		assertEquals(3, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, hemmed);

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(homestead));
		assertEquals(3, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, hemmed);
	}
}
