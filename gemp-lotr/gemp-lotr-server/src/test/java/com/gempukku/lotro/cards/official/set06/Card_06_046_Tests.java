package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stole", "6_46");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum], strength 5; a regroup ability used only on purpose
					put("wgollum", "103_66");   // Gollum, Half a Wraith Himself: the [wraith] Gollum; his abilities need a Nazgul played or threats
					put("shelob", "8_25");      // Shelob, Eater of Light: a [gollum] minion who is no Gollum; fierce, strength 8; cheated in
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; cheated in, so its text never comes up
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; cheated in
					put("pippin", "103_31");    // Pippin, Trollslayer: a Hobbit companion who is [gondor]; cheated in
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally who may be made to skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheyStoleItStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: They Stole It
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * Skirmish: Spot Gollum and discard 3 cards from hand to wound a companion Gollum is skirmishing once (or
		 * twice if that companion is a [shire] companion).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stole");

		assertEquals("They Stole It", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SkirmishAbilityDiscardsThreeCardsToWoundACompanionGollumIsSkirmishingOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// A Hobbit who is not [shire]: wounded once, not twice.
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);

		var stole = scn.GetShadowCard("stole");
		var gollum = scn.GetShadowCard("gollum");
		// The Shadow player's own copies, of four cultures and both sides.
		var hand1 = scn.GetShadowCard("runner");
		var hand2 = scn.GetShadowCard("aragorn");
		var hand3 = scn.GetShadowCard("merry");
		var hand4 = scn.GetShadowCard("thrarin");

		scn.StartGame();
		scn.MoveCardsToSupportArea(stole);
		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToHand(hand1, hand2, hand3, hand4);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, gollum);
		scn.FreepsResolveSkirmish(pippin);
		scn.FreepsPass();

		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.ShadowActionAvailable(stole));
		scn.ShadowUseCardAction(stole);

		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(hand1, hand2, hand3, hand4));
		scn.ShadowChooseCards(hand1, hand2, hand3);

		assertInDiscard(hand1, hand2, hand3);
		assertInHand(hand4);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AShireHobbitGollumIsSkirmishingIsWoundedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);

		var stole = scn.GetShadowCard("stole");
		var gollum = scn.GetShadowCard("gollum");
		var hand1 = scn.GetShadowCard("runner");
		var hand2 = scn.GetShadowCard("aragorn");
		var hand3 = scn.GetShadowCard("pippin");
		var hand4 = scn.GetShadowCard("thrarin");

		scn.StartGame();
		scn.MoveCardsToSupportArea(stole);
		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToHand(hand1, hand2, hand3, hand4);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, gollum);
		scn.FreepsResolveSkirmish(merry);
		scn.FreepsPass();

		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.ShadowUseCardAction(stole);
		scn.ShadowChooseCards(hand1, hand2, hand3);

		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AShireCompanionWhoIsNoHobbitIsWoundedTwiceAndTheWraithGollumIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		var stole = scn.GetShadowCard("stole");
		var wgollum = scn.GetShadowCard("wgollum");
		var hand1 = scn.GetShadowCard("runner");
		var hand2 = scn.GetShadowCard("aragorn");
		var hand3 = scn.GetShadowCard("pippin");
		var hand4 = scn.GetShadowCard("thrarin");

		scn.StartGame();
		scn.MoveCardsToSupportArea(stole);
		scn.MoveMinionsToTable(wgollum);
		scn.MoveCardsToHand(hand1, hand2, hand3, hand4);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, wgollum);
		scn.FreepsResolveSkirmish(smeagol);
		scn.FreepsPass();

		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.ShadowActionAvailable(stole));
		scn.ShadowUseCardAction(stole);
		scn.ShadowChooseCards(hand1, hand2, hand3);

		assertEquals(2, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoGollumItIsNotOfferedThoughAGollumMinionSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var stole = scn.GetShadowCard("stole");
		var shelob = scn.GetShadowCard("shelob");
		var hand1 = scn.GetShadowCard("runner");
		var hand2 = scn.GetShadowCard("merry");
		var hand3 = scn.GetShadowCard("pippin");
		var hand4 = scn.GetShadowCard("thrarin");

		scn.StartGame();
		scn.MoveCardsToSupportArea(stole);
		scn.MoveMinionsToTable(shelob);
		scn.MoveCardsToHand(hand1, hand2, hand3, hand4);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(stole));
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void WithTwoCardsInHandItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var stole = scn.GetShadowCard("stole");
		var gollum = scn.GetShadowCard("gollum");
		var hand1 = scn.GetShadowCard("runner");
		var hand2 = scn.GetShadowCard("merry");

		scn.StartGame();
		scn.MoveCardsToSupportArea(stole);
		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToHand(hand1, hand2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(stole));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void UsedWhileGollumWaitsForAnotherSkirmishItIsPaidForAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);

		var stole = scn.GetShadowCard("stole");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var hand1 = scn.GetShadowCard("aragorn");
		var hand2 = scn.GetShadowCard("merry");
		var hand3 = scn.GetShadowCard("pippin");
		var hand4 = scn.GetShadowCard("thrarin");

		scn.StartGame();
		scn.MoveCardsToSupportArea(stole);
		scn.MoveMinionsToTable(gollum, runner);
		scn.MoveCardsToHand(hand1, hand2, hand3, hand4);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{merry, gollum});
		// Aragorn's skirmish first: Merry is assigned to Gollum, and not yet skirmishing him.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Gollum can be spotted and the cost paid; the companion is part of the effect.
		assertEquals(4, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(stole));
		scn.ShadowUseCardAction(stole);
		scn.ShadowChooseCards(hand1, hand2, hand3);

		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllyGollumIsSkirmishingIsNotWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var stole = scn.GetShadowCard("stole");
		var gollum = scn.GetShadowCard("gollum");
		var hand1 = scn.GetShadowCard("runner");
		var hand2 = scn.GetShadowCard("aragorn");
		var hand3 = scn.GetShadowCard("merry");
		var hand4 = scn.GetShadowCard("pippin");

		scn.StartGame();
		scn.MoveCardsToSupportArea(stole);
		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToHand(hand1, hand2, hand3, hand4);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes until the regroup phase.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, gollum);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, gollum));
		assertEquals(4, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(stole));
		scn.ShadowUseCardAction(stole);
		scn.ShadowChooseCards(hand1, hand2, hand3);

		// Thrarin, with one vitality left, is still there and unwounded by it.
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertInZone(Zone.SUPPORT, thrarin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
