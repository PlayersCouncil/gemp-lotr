package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scavenger", "10_54");
					put("scavenger2", "10_54"); // a second copy: not unique, and "this minion" is the one that won
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; a play trigger only
					put("scimitar", "1_180");   // Goblin Scimitar: a possession (hand weapon, +2) on a [moria] Orc; a play trigger only
					put("scout", "1_191");      // Moria Scout: [moria] Orc, strength 6; a play trigger only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("cloak", "1_42");       // Elven Cloak: an [elven] possession, cloak, on a companion; minion archery total -1
					put("mail", "1_101");       // Coat of Mail: a [gondor] possession, armor, on a Man; bearer not overwhelmed unless tripled
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: an ARTIFACT hand weapon on a [gondor] Man; its response is used only on purpose
					put("merry", "1_302");      // Merry, Friend to Sam: a skirmish ability used only on purpose
					put("hobbitsword", "1_299"); // Hobbit Sword: a possession (hand weapon, +2) on a Hobbit, no other text
					put("toby", "1_305");       // Old Toby: a Free Peoples possession in the support area, borne by nobody
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability used only on purpose
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: a Hobbit ally, strength 2, vitality 3
					put("follow", "1_109");     // One Whom Men Would Follow: exert Aragorn to have an ally take part in skirmishes, strength +2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CirithUngolScavengerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Cirith Ungol Scavenger
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Each time this minion wins a skirmish, you may exert it to discard a possession borne by a character this minion is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scavenger");

		assertEquals("Cirith Ungol Scavenger", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningASkirmishItMayExertToDiscardAPossessionBorneByTheCompanionItIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scavenger = scn.GetShadowCard("scavenger");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		var scout = scn.GetShadowCard("scout");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		var mail = scn.GetFreepsCard("mail");
		var narsil = scn.GetFreepsCard("narsil");
		var merry = scn.GetFreepsCard("merry");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.AttachCardsTo(aragorn, cloak, mail, narsil);
		scn.AttachCardsTo(merry, hobbitsword);
		scn.MoveCardsToSupportArea(toby);
		assertAttachedTo(cloak, aragorn);
		assertAttachedTo(mail, aragorn);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(hobbitsword, merry);
		assertInZone(Zone.SUPPORT, toby);

		scn.StartGame();
		scn.MoveMinionsToTable(scavenger, runner, scout);
		scn.AttachCardsTo(runner, scimitar);
		assertAttachedTo(scimitar, runner);

		scn.SkipToAssignments();
		// Merry's skirmish has not begun when the Scavenger's is won.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, scavenger}, new PhysicalCardImpl[]{merry, scout});
		// The Runner is a second minion in Aragorn's skirmish, bearing a possession of his own.
		scn.ShadowAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, scavenger));
		assertEquals(0, scn.GetWoundsOn(scavenger));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Aragorn 8 against 8 + 5 + 2: the minions win, and the Coat of Mail keeps him from being overwhelmed.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scavenger));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(scavenger));

		// Two possessions of other cultures and classes on the companion it skirmishes; not the artifact beside
		// them, a possession on a minion in the same skirmish, one on a companion whose skirmish is still to come,
		// or one that nobody bears.
		assertTrue(scn.ShadowHasCardChoiceAvailable(cloak, mail));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(narsil, scimitar, hobbitsword, toby));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(cloak);

		assertInDiscard(cloak);
		assertAttachedTo(mail, aragorn);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(scimitar, runner);
		assertAttachedTo(hobbitsword, merry);
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish"));
	}

	@Test
	public void APossessionBorneByAnAllyItIsSkirmishingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scavenger = scn.GetShadowCard("scavenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var maggot = scn.GetFreepsCard("maggot");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var follow = scn.GetFreepsCard("follow");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);
		scn.AttachCardsTo(maggot, hobbitsword);
		scn.MoveCardsToHand(follow);
		assertAttachedTo(hobbitsword, maggot);

		scn.StartGame();
		scn.MoveMinionsToTable(scavenger);

		// Set-up: Maggot takes part in skirmishes, strength 2 + 2 + 2.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(follow);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(maggot, scavenger);
		scn.FreepsResolveSkirmish(maggot);
		assertTrue(scn.IsCharSkirmishingAgainst(maggot, scavenger));
		assertEquals(0, scn.GetWoundsOn(scavenger));
		assertEquals(0, scn.GetWoundsOn(maggot));
		scn.PassSkirmishActions();

		// 8 against 6: Maggot loses and lives.
		assertEquals(1, scn.GetWoundsOn(maggot));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scavenger));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(scavenger));
		assertInDiscard(hobbitsword);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningAgainstACompanionWhoBearsNoPossessionItMayExertForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scavenger = scn.GetShadowCard("scavenger");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(boromir, aragorn);
		// Aragorn's cloak: a possession in play, borne by a companion who is in no skirmish.
		scn.AttachCardsTo(aragorn, cloak);
		assertAttachedTo(cloak, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(scavenger);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, scavenger);
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(0, scn.GetWoundsOn(scavenger));
		scn.PassSkirmishActions();

		// 8 against 7. The trigger has a cost, so it is offered though nothing can be discarded.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scavenger));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(scavenger));
		assertAttachedTo(cloak, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenItLosesAndAnotherCopyWinningMakesOnlyItsOwnOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scavenger = scn.GetShadowCard("scavenger");
		var scavenger2 = scn.GetShadowCard("scavenger2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var mail = scn.GetFreepsCard("mail");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, cloak);
		scn.AttachCardsTo(boromir, mail);
		assertAttachedTo(cloak, aragorn);
		assertAttachedTo(mail, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(scavenger, scavenger2);
		// Aragorn at 12 beats the Scavenger without overwhelming it (16), so it lives through the loss.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, scavenger}, new PhysicalCardImpl[]{boromir, scavenger2});
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(scavenger));
		scn.PassSkirmishActions();

		// It lost: nothing is offered, and the game goes on to the next skirmish.
		assertEquals(1, scn.GetWoundsOn(scavenger));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish"));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertAttachedTo(cloak, aragorn);

		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// The other copy wins against Boromir: its own offer only, not the copy that lost.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scavenger2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scavenger));
		scn.ShadowDeclineOptionalTrigger();
		assertAttachedTo(mail, boromir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinIsOfferedAgainAndDecliningChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scavenger = scn.GetShadowCard("scavenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		var mail = scn.GetFreepsCard("mail");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, cloak, mail);
		assertAttachedTo(cloak, aragorn);
		assertAttachedTo(mail, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(scavenger);
		// Fierce, so that it skirmishes, and wins, a second time in the turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, scavenger, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, scavenger);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 8 against 8: the Scavenger wins.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scavenger));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(scavenger));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertAttachedTo(cloak, aragorn);
		assertAttachedTo(mail, aragorn);

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, scavenger);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scavenger));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(scavenger));
		scn.ShadowChooseCard(mail);

		assertInDiscard(mail);
		assertAttachedTo(cloak, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenItWinsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scavenger = scn.GetShadowCard("scavenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, cloak);
		assertAttachedTo(cloak, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(scavenger);
		// Vitality 2: with 1 wound it cannot exert.
		scn.AddWoundsToChar(scavenger, 1);
		assertEquals(1, scn.GetWoundsOn(scavenger));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, scavenger);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 8 against 8: it wins, and the game goes on to the regroup phase with nothing offered.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(scavenger));
		assertAttachedTo(cloak, aragorn);
	}
}
