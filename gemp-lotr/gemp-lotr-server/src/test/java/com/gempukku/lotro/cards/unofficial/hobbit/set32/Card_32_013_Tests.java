package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_013_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The skirmishes of
	// turn 1 are at The Troll Hoard (site 2), away from Galadriel's home (3H, Rivendell 30_51).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "32_13");
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion, Wise, 7/4; a fellowship ability, a discard
					// bar that needs 2 [dwarven] followers, and a discount on Thorin
					put("gandalf", "30_26");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, not Wise, 7/3; a fellowship ability only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; a bonus only with 3 Ents
					// Radagast, The Brown: [gandalf] Wizard ally, Wise; a fellowship ability that wants a Gandalf in
					// the discard pile
					put("radagast", "31_17");

					// Cards for the hand: three Shadow cards of three kinds and a Free Peoples card
					put("footman", "30_42");    // Goblin Footman: a [moria] minion
					put("hatred", "30_34");     // Hatred Rekindled: a [gundabad] condition
					put("scimitar", "31_31");   // Goblin Scimitar: a [moria] possession
					put("takeup", "30_31");     // Take up arms. Fight!: a [gandalf] event

					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, 7/2; archer only at site 9, a regroup ability not used
					put("troll", "32_31");      // Demolition Troll: [moria] Troll, 10/3; a skirmish ability not used
					// Assaulting Goblin: [moria] Orc, 7/2; a response to an Orc's wound at a battleground (none laid)
					put("goblin", "32_27");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Galadriel, Elven Lady
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Ally
		 * Subtype: Elf
		 * Home: 3H
		 * Strength: 3
		 * Vitality: 3
		 * Game Text: <b>Wise</b>.<br>At the start of each of your turns, you may heal Gandalf.<br><b>Skirmish:</b> Exert Galadriel and discard a Shadow card from hand to make a minion strength -2 (or -3 if a Wise character is skirmishing that minion).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("galadriel");

		assertEquals("Galadriel", card.getBlueprint().getTitle());
		assertEquals("Elven Lady", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.WISE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 3)));
	}

	@Test
	public void AtTheStartOfHerOwnersTurnSheMayHealGandalfAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveAlliesToTable(galadriel, radagast);
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.AddWoundsToChar(gandalf, 2);
		scn.AddWoundsToChar(avenger, 1);
		scn.AddWoundsToChar(radagast, 1);
		scn.AddWoundsToChar(galadriel, 1);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertEquals(1, scn.GetWoundsOn(galadriel));

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		scn.FreepsAcceptOptionalTrigger();

		// One wound of Gandalf's two, unasked; not the [gandalf] companion who is no Gandalf, the Wise ally, or
		// Galadriel herself.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithGandalfUnwoundedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveAlliesToTable(galadriel, radagast);
		scn.MoveCompanionsToTable(gandalf, avenger);
		// Wounded, but neither is Gandalf.
		scn.AddWoundsToChar(avenger, 1);
		scn.AddWoundsToChar(radagast, 1);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertEquals(1, scn.GetWoundsOn(radagast));

		scn.StartGame();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesGandalfWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCompanionsToTable(gandalf);
		scn.AddWoundsToChar(gandalf, 1);
		assertEquals(1, scn.GetWoundsOn(gandalf));

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOfferComesAgainAtHerOwnersNextTurnAndNotAtTheOtherPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCompanionsToTable(gandalf);
		scn.AddWoundsToChar(gandalf, 2);
		assertEquals(2, scn.GetWoundsOn(gandalf));

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Turn 2, the other player's: nothing is offered to Galadriel's owner.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		assertEquals(2, scn.GetWoundsOn(gandalf));

		// Turn 3, her owner's: offered again, and taken.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// The Troll Hoard (site 2) makes no offer of its own: no hand weapon the Free Peoples player could play (F-0800).
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
	}

	@Test
	public void SkirmishAbilityMakesAMinionSkirmishingAWiseCompanionStrengthMinus3ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		var footman = scn.GetFreepsCard("footman");
		var hatred = scn.GetFreepsCard("hatred");
		var scimitar = scn.GetFreepsCard("scimitar");
		var takeup = scn.GetFreepsCard("takeup");
		var veteran = scn.GetShadowCard("veteran");
		var troll = scn.GetShadowCard("troll");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.MoveCardsToHand(footman, hatred, scimitar, takeup);
		assertInHand(footman, hatred, scimitar, takeup);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, troll, goblin);
		scn.SkipToAssignments();
		// The Assaulting Goblin is left unassigned.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, veteran}, new PhysicalCardImpl[]{balin, troll});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);

		assertInHand(footman, hatred, scimitar, takeup);
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(7, scn.GetStrength(veteran));
		assertEquals(10, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(goblin));
		assertTrue(scn.FreepsActionAvailable(galadriel));
		scn.FreepsUseCardAction(galadriel);

		assertEquals(1, scn.GetWoundsOn(galadriel));
		// Shadow cards of three kinds may be discarded; not a Free Peoples card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(footman, hatred, scimitar));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(takeup));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(hatred);
		assertInDiscard(hatred);
		assertInHand(footman, scimitar, takeup);

		// Any minion: in this skirmish, waiting for another, or in none; no companion or ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(veteran, troll, goblin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, balin, bilbo, galadriel));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(veteran);

		// 7 - 3: Gandalf, Wise, is skirmishing him.
		assertEquals(4, scn.GetStrength(veteran));
		assertEquals(10, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(goblin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gandalf's skirmish resolves; in Balin's, the next, the Veteran is himself again.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(veteran));
	}

	@Test
	public void AMinionNoWiseCharacterIsSkirmishingIsMadeStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var footman = scn.GetFreepsCard("footman");
		var hatred = scn.GetFreepsCard("hatred");
		var veteran = scn.GetShadowCard("veteran");
		var troll = scn.GetShadowCard("troll");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.MoveCardsToHand(footman, hatred);
		assertInHand(footman, hatred);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, troll, goblin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, veteran}, new PhysicalCardImpl[]{balin, troll});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);

		assertInHand(footman, hatred);
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(7, scn.GetStrength(veteran));
		assertEquals(10, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(goblin));

		// In Gandalf's skirmish, the Troll waiting for Balin: a Wise character is skirmishing, but not him.
		scn.FreepsUseCardAction(galadriel);
		scn.FreepsChooseCard(hatred);
		scn.FreepsChooseCard(troll);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(8, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(veteran));

		scn.ShadowPass();
		// The Goblin, in no skirmish.
		scn.FreepsUseCardAction(galadriel);
		// The Footman is the one Shadow card left in hand, discarded unasked.
		scn.FreepsChooseCard(goblin);
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertEquals(5, scn.GetStrength(goblin));
		assertInDiscard(footman);
		assertEquals(8, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(veteran));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingACompanionWhoIsNotWiseIsMadeStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var hatred = scn.GetFreepsCard("hatred");
		var veteran = scn.GetShadowCard("veteran");
		var troll = scn.GetShadowCard("troll");
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.MoveCardsToHand(hatred);
		assertInHand(hatred);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, troll);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, veteran}, new PhysicalCardImpl[]{balin, troll});
		scn.FreepsResolveSkirmish(balin);

		assertInHand(hatred);
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(10, scn.GetStrength(troll));

		scn.FreepsUseCardAction(galadriel);
		// The Hatred is the one Shadow card in hand, discarded unasked.
		scn.FreepsChooseCard(troll);

		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertInDiscard(hatred);
		// 10 - 2: Balin is no Wise character.
		assertEquals(8, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(veteran));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtHerHomeSiteGaladrielSkirmishingAMinionMakesItStrengthMinus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var hatred = scn.GetFreepsCard("hatred");
		var veteran = scn.GetShadowCard("veteran");
		scn.MoveAlliesToTable(galadriel);

		scn.StartGame();
		scn.SkipToSite(2);
		// The Troll Hoard's own offer at the start of the fellowship phase.
		scn.FreepsDeclineOptionalTrigger();
		scn.MoveMinionsToTable(veteran);
		scn.MoveCardsToHand(hatred);
		assertInHand(hatred);
		assertEquals(0, scn.GetWoundsOn(galadriel));

		// The move to Rivendell, her home site: she may be assigned.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(galadriel, veteran);
		scn.FreepsResolveSkirmish(galadriel);
		assertEquals(7, scn.GetStrength(veteran));

		scn.FreepsUseCardAction(galadriel);
		scn.FreepsChooseCard(hatred);
		// The Veteran, the one minion in play, is chosen unasked.

		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertInDiscard(hatred);
		// 7 - 3: Galadriel, a Wise ally, is skirmishing him.
		assertEquals(4, scn.GetStrength(veteran));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileGaladrielIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var hatred = scn.GetFreepsCard("hatred");
		var veteran = scn.GetShadowCard("veteran");
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(hatred);
		assertInHand(hatred);
		// Vitality 3 with 2 wounds: she cannot exert.
		scn.AddWoundsToChar(galadriel, 2);
		assertEquals(2, scn.GetWoundsOn(galadriel));

		scn.StartGame();
		scn.MoveMinionsToTable(veteran);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, veteran);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(galadriel));
		assertInHand(hatred);
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertEquals(7, scn.GetStrength(veteran));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoShadowCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var takeup = scn.GetFreepsCard("takeup");
		var veteran = scn.GetShadowCard("veteran");
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(takeup);
		assertInHand(takeup);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, veteran);
		scn.FreepsResolveSkirmish(gandalf);

		// Only a Free Peoples card in hand.
		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(takeup);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(galadriel));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(7, scn.GetStrength(veteran));
	}
}
