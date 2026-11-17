package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_007_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stash", "103_7");
					put("gandalf", "1_364");        // Gandalf, The Grey Wizard: strength 7, vitality 4; a fellowship ability never used here
					put("radagast", "9_26");        // Radagast, The Brown: the other Free Peoples Wizard, strength 8, vitality 4
					put("quickbeam", "6_33");       // Quickbeam, Bregalad: a [gandalf] companion who is an Ent and no Wizard
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] Man, strength 8
					put("sam", "1_311");            // Sam, Son of Hamfast: "Fellowship: Exert Sam to remove a burden."
					put("bounder", "1_286");        // Bounder: Hobbit ally, strength 2; a skirmish ability only
					put("gandalfspipe", "1_74");    // Bearer must be Gandalf. Fellowship: Discard a pipeweed possession and spot X pipes to remove X burdens.
					put("aragornspipe", "1_91");    // Bearer must be a [gondor] companion. Fellowship: ... to heal X companions.
					put("frodospipe", "3_107");     // Bearer must be Frodo. Fellowship: ... to heal a companion with the Frodo signet X times.
					put("bilbospipe", "1_285");     // Bearer must be a Hobbit. Fellowship: ... to shuffle X tales into your draw deck.
					put("toby1", "1_305");          // Old Toby: a pipeweed possession in the support area
					put("toby2", "1_305");
					put("leaf", "1_300");           // Longbottom Leaf: another pipeweed possession in the support area
					put("troop", "1_177");          // Goblin Patrol Troop: a minion with no text
					put("saruman", "3_69");         // Saruman, Servant of the Eye: an [isengard] minion who is a Wizard
					put("whitesaruman", "32_24");   // Saruman, The White (a Hobbit-set card): a [gandalf] ally who is a Wizard; fellowship and skirmish abilities never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WizardsStashStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Wizard's Stash
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype:
		 * Game Text: Pipeweed. Bearer must be a Wizard.
		 * 	Each companion who is bearing a pipe is strength +1.
		 * 	Regroup: Exert bearer and discard this. This phase you may use special abilities on pipes as if it were
		 * 	the Fellowship phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stash");

		assertEquals("Wizard's Stash", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedOnAWizardCompanionOrAllyAndOnNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stash = scn.GetFreepsCard("stash");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var whitesaruman = scn.GetFreepsCard("whitesaruman");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, radagast, quickbeam, aragorn);
		// A Wizard who is an ally: the text does not say "companion".
		scn.MoveAlliesToTable(whitesaruman);
		scn.MoveCardsToHand(stash);

		scn.StartGame();

		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(stash));

		scn.FreepsPlayCard(stash);

		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast, whitesaruman));
		// Quickbeam is [gandalf] and no Wizard; Aragorn and Frodo are neither.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(quickbeam, aragorn, frodo));
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(radagast);

		assertEquals(0, scn.GetFreepsHandCount());
		assertAttachedTo(stash, radagast);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AWizardWhoIsAMinionMayBearItToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stash = scn.GetFreepsCard("stash");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(stash);

		scn.StartGame();
		// The text says "a Wizard", not "your Wizard" and not "a companion". A minion is in play beside a Stash
		// being played only when Merry's Pipe (103_113) plays it back from the discard pile in the regroup
		// phase; the minions are put down in the fellowship phase here to ask the same thing the short way.
		scn.MoveMinionsToTable(saruman, troop);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(stash);

		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, saruman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));

		scn.FreepsChooseCard(saruman);

		assertAttachedTo(stash, saruman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachCompanionBearingAPipeIsStrengthPlusOneAndNobodyElseIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stash = scn.GetFreepsCard("stash");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var frodospipe = scn.GetFreepsCard("frodospipe");
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		scn.MoveCompanionsToTable(gandalf, radagast, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(aragorn, aragornspipe);
		scn.AttachCardsTo(frodo, frodospipe);
		// An ally bearing a pipe: the text says "companion".
		scn.AttachCardsTo(bounder, bilbospipe);
		scn.MoveCardsToHand(stash, gandalfspipe);

		scn.StartGame();

		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(radagast));
		assertEquals(8, scn.GetStrength(aragorn));
		// Strength 3 + 1 for The One Ring
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(2, scn.GetStrength(bounder));

		scn.FreepsPlayCard(stash);
		scn.FreepsChooseCard(gandalf);

		assertAttachedTo(stash, gandalf);
		// A [gondor] Man and a Hobbit who is the Ring-bearer, each bearing a pipe
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(frodo));
		// Gandalf bears the Stash, which is pipeweed and no pipe
		assertEquals(7, scn.GetStrength(gandalf));
		// A companion bearing nothing
		assertEquals(8, scn.GetStrength(radagast));
		// A pipe on an ally
		assertEquals(2, scn.GetStrength(bounder));

		// Gandalf's Pipe costs nothing and has one possible bearer.
		scn.FreepsPlayCard(gandalfspipe);

		assertAttachedTo(gandalfspipe, gandalf);
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(radagast));
		assertEquals(2, scn.GetStrength(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsItsBearerDiscardsItselfAndOpensTheFellowshipAbilitiesOfPipesOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stash = scn.GetFreepsCard("stash");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var toby1 = scn.GetFreepsCard("toby1");
		var toby2 = scn.GetFreepsCard("toby2");
		scn.MoveCompanionsToTable(gandalf, radagast, aragorn, sam);
		scn.AttachCardsTo(gandalf, stash, gandalfspipe);
		scn.AttachCardsTo(aragorn, aragornspipe);
		scn.MoveCardsToSupportArea(toby1, toby2);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.FreepsPass();
		// No minion is in play, so the turn goes from the Shadow phase straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertAttachedTo(stash, gandalf);
		// Strength 8 + 1 for bearing a pipe while the Stash is in play
		assertEquals(9, scn.GetStrength(aragorn));
		// Before the Stash is used, no Fellowship ability is on offer in the regroup phase.
		assertFalse(scn.FreepsActionAvailable(gandalfspipe));
		assertFalse(scn.FreepsActionAvailable(aragornspipe));
		assertFalse(scn.FreepsActionAvailable(sam));
		assertTrue(scn.FreepsActionAvailable(stash));

		scn.FreepsUseCardAction(stash);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertInZone(Zone.DISCARD, stash);
		// The other pipeweed and the bearer's pipe stay where they were.
		assertInZone(Zone.SUPPORT, toby1, toby2);
		assertAttachedTo(gandalfspipe, gandalf);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		scn.ShadowPass();

		// A pipe on the Wizard who bore the Stash and a pipe of another culture on another companion
		assertTrue(scn.FreepsActionAvailable(gandalfspipe));
		assertTrue(scn.FreepsActionAvailable(aragornspipe));
		// A Fellowship ability that is not on a pipe
		assertFalse(scn.FreepsActionAvailable(sam));

		scn.FreepsUseCardAction(gandalfspipe);
		assertTrue(scn.FreepsHasCardChoiceAvailable(toby1, toby2));
		scn.FreepsChooseCard(toby1);
		assertInZone(Zone.DISCARD, toby1);
		// Two pipes can be spotted: Gandalf's and Aragorn's.
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");

		// 3 - 2
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, toby2);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void StashAbilityIsNotOfferedInTheFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stash = scn.GetFreepsCard("stash");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var toby1 = scn.GetFreepsCard("toby1");
		scn.MoveCompanionsToTable(gandalf, sam);
		scn.AttachCardsTo(gandalf, stash, gandalfspipe);
		scn.MoveCardsToSupportArea(toby1);

		scn.StartGame();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		// The Fellowship abilities are on offer, the pipe's and Sam's; the Stash's own is a Regroup ability.
		assertTrue(scn.FreepsActionAvailable(gandalfspipe));
		assertTrue(scn.FreepsActionAvailable(sam));
		assertFalse(scn.FreepsActionAvailable(stash));
		assertAttachedTo(stash, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void StashAbilityIsNotOfferedWhileItsBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stash = scn.GetFreepsCard("stash");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var toby1 = scn.GetFreepsCard("toby1");
		// Radagast could exert, but he is not the bearer.
		scn.MoveCompanionsToTable(gandalf, radagast);
		scn.AttachCardsTo(gandalf, stash, gandalfspipe);
		scn.MoveCardsToSupportArea(toby1);

		scn.StartGame();
		// Vitality 4 with 3 wounds
		scn.AddWoundsToChar(gandalf, 3);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.IsExhausted(gandalf));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertFalse(scn.FreepsActionAvailable(stash));
		assertFalse(scn.FreepsActionAvailable(gandalfspipe));
		assertAttachedTo(stash, gandalf);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PipeAbilitiesStayOpenForThatRegroupPhaseAndNoLongerAndNoneHasToBeUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stash = scn.GetFreepsCard("stash");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var toby1 = scn.GetFreepsCard("toby1");
		var toby2 = scn.GetFreepsCard("toby2");
		var leaf = scn.GetFreepsCard("leaf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, stash, gandalfspipe);
		// Three pipeweed possessions: two to pay for the pipe twice, and one left over so that the pipe's cost
		// can still be paid when the ability is looked for afterwards.
		scn.MoveCardsToSupportArea(toby1, toby2, leaf);

		scn.StartGame();
		scn.AddBurdens(4);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.FreepsActionAvailable(gandalfspipe));

		scn.FreepsUseCardAction(stash);
		assertInZone(Zone.DISCARD, stash);
		scn.ShadowPass();

		assertTrue(scn.FreepsActionAvailable(gandalfspipe));
		scn.FreepsUseCardAction(gandalfspipe);
		scn.FreepsChooseCard(toby1);
		// One pipe in play, so X is 1 at most: 4 - 1
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");
		assertEquals(3, scn.GetBurdens());
		scn.ShadowPass();

		// Still open after a pipe has been used and the Shadow player has had his turn to act.
		assertTrue(scn.FreepsActionAvailable(gandalfspipe));
		scn.FreepsUseCardAction(gandalfspipe);
		scn.FreepsChooseCard(toby2);
		scn.FreepsChoose("1");
		assertEquals(2, scn.GetBurdens());
		scn.ShadowPass();

		// Still open at the last decision of the phase, and the player is free to leave it unused.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsActionAvailable(gandalfspipe));
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, leaf);
		assertEquals(2, scn.GetCurrentSiteNumber());

		// The fellowship moves again. No minion is in play, so the turn goes from the Shadow phase straight to
		// a second regroup phase: the first regroup phase has ended and no maneuver, archery, assignment or
		// skirmish phase has come and gone since.
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);

		// The second regroup phase of the same turn: "this phase" was the first.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertInZone(Zone.SUPPORT, leaf);
		assertAttachedTo(gandalfspipe, gandalf);
		assertFalse(scn.FreepsActionAvailable(gandalfspipe));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
