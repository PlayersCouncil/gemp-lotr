package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("jetsam1", "103_2");
					put("jetsam2", "103_2");
					put("jetsam3", "103_2");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength modifier and nothing else
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability never used here
					put("bounder", "1_286");    // Bounder: Hobbit ally; a skirmish ability
					put("guardian1", "5_17");   // Forest Guardian: Ent companion, not unique, unhasty; an assignment ability never used here
					put("guardian2", "5_17");
					put("treebeard", "4_103");  // Treebeard, Earthborn: Ent ally, unhasty; his abilities are about unbound Hobbits

					// "Fellowship: Discard a pipeweed possession and spot X pipes to ..."
					put("gpipe", "1_74");       // Gandalf's Pipe: cost 0, bearer Gandalf; ... remove X burdens
					put("apipe", "1_91");       // Aragorn's Pipe: bearer a [gondor] companion; ... heal X companions
					put("bpipe", "1_285");      // Bilbo's Pipe: cost 1, bearer a Hobbit; ... shuffle X tales into the draw deck

					put("toby1", "1_305");      // Old Toby: pipeweed possession, support area; may draw a card when played
					put("toby2", "1_305");
					put("toby3", "1_305");
					put("sword", "1_299");      // Hobbit Sword: a possession that is no pipe
					// Long-stemmed Pipe: a pipeweed condition and, for all its name, no pipe.
					// "Regroup: Discard another pipeweed from play to discard a condition. Discard this condition."
					put("longstem", "17_21");
					// Wizard's Stash: pipeweed possession, bearer a Wizard. "Regroup: Exert bearer and discard this. This
					// phase you may use special abilities on pipes as if it were the Fellowship phase."
					put("stash", "103_7");
					// Shadowfax, Greatest of the Mearas: a [gandalf] possession and no pipe, bearer a [gandalf] Wizard.
					// "Discard all other possessions on bearer"; a fellowship ability never used here.
					put("shadowfax", "17_24");

					put("breath1", "1_207");    // Black Breath: [wraith] condition; on a character it stops healing and nothing else
					put("breath2", "1_207");
					put("poison", "8_28");      // Spider Poison: [gollum] possession; bearer exerts when assigned to a skirmish
					put("troop", "1_177");      // Goblin Patrol Troop: no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void JetsamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Jetsam
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype:
		 * Game Text: Pipeweed. Bearer must bear a pipe. When you play this, you may discard a Shadow card on bearer.
		 * 	When this is discarded by a pipe, add 1 to the number of pipes you can spot this phase and make your
		 * 	character lose <b>unhasty</b> until the end of the turn.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("jetsam1");

		assertEquals("Jetsam", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(0, card.getBlueprint().getStrength());
		assertEquals(0, card.getBlueprint().getVitality());
	}

	@Test
	public void PlayedForOneOnACharacterBearingAPipeAndItsDiscardMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var jetsam = scn.GetFreepsCard("jetsam1");
		var otherJetsam = scn.GetFreepsCard("jetsam3");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var gpipe = scn.GetFreepsCard("gpipe");
		var bpipe = scn.GetFreepsCard("bpipe");
		var sword = scn.GetFreepsCard("sword");
		var breath = scn.GetShadowCard("breath1");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(bounder);
		// Frodo bears a possession and a pipeweed, and no pipe.
		scn.AttachCardsTo(frodo, sword, otherJetsam);
		scn.AttachCardsTo(bounder, breath);
		scn.MoveCardsToHand(jetsam, gpipe, bpipe);

		scn.StartGame();

		// Nobody bears a pipe.
		assertFalse(scn.FreepsPlayAvailable(jetsam));

		// Gandalf's Pipe costs nothing and goes on Gandalf.
		scn.FreepsPlayCard(gpipe);
		assertAttachedTo(gpipe, gandalf);
		assertTrue(scn.FreepsPlayAvailable(jetsam));

		scn.FreepsPlayCard(bpipe);
		scn.FreepsChooseCard(bounder);
		assertAttachedTo(bpipe, bounder);
		assertEquals(1, scn.GetTwilight());

		scn.FreepsPlayCard(jetsam);

		// A [gandalf] companion and a [shire] ally, each with a pipe.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(bounder);

		assertAttachedTo(jetsam, bounder);
		// 1 for Bilbo's Pipe + 1 for Jetsam
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(jetsam));

		scn.FreepsDeclineOptionalTrigger();

		assertAttachedTo(breath, bounder);
		assertAttachedTo(jetsam, bounder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenPlayedItMayDiscardAShadowCardOnItsOwnBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jetsam = scn.GetFreepsCard("jetsam1");
		var earlierJetsam = scn.GetFreepsCard("jetsam2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gpipe = scn.GetFreepsCard("gpipe");
		var apipe = scn.GetFreepsCard("apipe");
		var breathOnGandalf = scn.GetShadowCard("breath1");
		var breathOnAragorn = scn.GetShadowCard("breath2");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, gpipe, earlierJetsam);
		scn.AttachCardsTo(aragorn, apipe);
		// A [wraith] condition and a [gollum] possession on Gandalf, and a Shadow card on the other pipe-bearer.
		scn.AttachCardsTo(gandalf, breathOnGandalf, poison);
		scn.AttachCardsTo(aragorn, breathOnAragorn);
		scn.MoveCardsToHand(jetsam);

		scn.StartGame();

		scn.FreepsPlayCard(jetsam);
		scn.FreepsChooseCard(gandalf);

		assertAttachedTo(jetsam, gandalf);
		// The copy just played makes the offer; the copy that was there already makes none.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(jetsam));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(earlierJetsam));

		scn.FreepsChooseOptionalTrigger(jetsam);

		assertTrue(scn.FreepsHasCardChoiceAvailable(breathOnGandalf, poison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(breathOnAragorn, gpipe, earlierJetsam, jetsam));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(poison);

		assertInZone(Zone.DISCARD, poison);
		assertAttachedTo(breathOnGandalf, gandalf);
		assertAttachedTo(breathOnAragorn, aragorn);
		assertAttachedTo(jetsam, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHinderedShadowCardOnBearerMayBeTheOneDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jetsam = scn.GetFreepsCard("jetsam1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gpipe = scn.GetFreepsCard("gpipe");
		var breath = scn.GetShadowCard("breath1");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, gpipe, breath, poison);
		// Both Shadow cards on Gandalf are hindered: neither has a type or a culture, and each is a Shadow card still.
		scn.HinderCard(breath, poison);
		scn.MoveCardsToHand(jetsam);

		scn.StartGame();

		assertTrue(scn.IsHindered(breath));
		assertTrue(scn.IsHindered(poison));

		scn.FreepsPlayCard(jetsam);

		assertAttachedTo(jetsam, gandalf);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(jetsam));

		scn.FreepsChooseOptionalTrigger(jetsam);

		assertTrue(scn.FreepsHasCardChoiceAvailable(breath, poison));
		scn.FreepsChooseCard(breath);

		assertInZone(Zone.DISCARD, breath);
		assertAttachedTo(poison, gandalf);
		assertTrue(scn.IsHindered(poison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenPlayedOnABearerWithNoShadowCardNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jetsam = scn.GetFreepsCard("jetsam1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gpipe = scn.GetFreepsCard("gpipe");
		var apipe = scn.GetFreepsCard("apipe");
		var breathOnAragorn = scn.GetShadowCard("breath2");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, gpipe);
		scn.AttachCardsTo(aragorn, apipe, breathOnAragorn);
		scn.MoveCardsToHand(jetsam);

		scn.StartGame();

		scn.FreepsPlayCard(jetsam);
		scn.FreepsChooseCard(gandalf);

		// The only Shadow card is on another character.
		assertAttachedTo(jetsam, gandalf);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(jetsam));
		assertAttachedTo(breathOnAragorn, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardedByAPipeItAddsOneToThePipesSpottedAndMakesACharacterLoseUnhasty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jetsam = scn.GetFreepsCard("jetsam1");
		var otherJetsam = scn.GetFreepsCard("jetsam2");
		var toby = scn.GetFreepsCard("toby1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gpipe = scn.GetFreepsCard("gpipe");
		var apipe = scn.GetFreepsCard("apipe");
		var guardian = scn.GetFreepsCard("guardian1");
		var otherGuardian = scn.GetFreepsCard("guardian2");
		var treebeard = scn.GetFreepsCard("treebeard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, aragorn, guardian, otherGuardian);
		scn.MoveAlliesToTable(treebeard);
		scn.AttachCardsTo(gandalf, gpipe, jetsam, otherJetsam);
		scn.AttachCardsTo(aragorn, apipe);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddBurdens(5);
		// An unhasty character who is not mine.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.MINION, null, Keyword.UNHASTY));

		assertTrue(scn.HasKeyword(guardian, Keyword.UNHASTY));
		assertTrue(scn.HasKeyword(otherGuardian, Keyword.UNHASTY));
		assertTrue(scn.HasKeyword(treebeard, Keyword.UNHASTY));
		assertTrue(scn.HasKeyword(troop, Keyword.UNHASTY));

		scn.FreepsUseCardAction(gpipe);
		assertTrue(scn.FreepsHasCardChoiceAvailable(jetsam, otherJetsam, toby));
		scn.FreepsChooseCard(jetsam);

		assertInZone(Zone.DISCARD, jetsam);
		assertAttachedTo(otherJetsam, gandalf);
		// An unhasty companion, a second copy of him, and an unhasty ally. Not the minion, who is no character of mine.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guardian, otherGuardian, treebeard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(guardian);

		assertFalse(scn.HasKeyword(guardian, Keyword.UNHASTY));
		assertTrue(scn.HasKeyword(otherGuardian, Keyword.UNHASTY));
		assertTrue(scn.HasKeyword(treebeard, Keyword.UNHASTY));
		assertTrue(scn.HasKeyword(troop, Keyword.UNHASTY));
		// 2 pipes in play + 1 for the Jetsam just discarded. The copy still on Gandalf adds nothing.
		assertEquals(3, scn.FreepsGetChoiceMax());

		scn.FreepsChoose("3");

		// 5 - 3
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A pipe of another culture discards the second copy.
		scn.FreepsUseCardAction(apipe);
		assertTrue(scn.FreepsHasCardChoiceAvailable(otherJetsam, toby));
		scn.FreepsChooseCard(otherJetsam);

		assertInZone(Zone.DISCARD, otherJetsam);
		// The Guardian who has lost unhasty is no longer among the unhasty.
		assertTrue(scn.FreepsHasCardChoiceAvailable(otherGuardian, treebeard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guardian, troop));

		scn.FreepsChooseCard(treebeard);

		assertFalse(scn.HasKeyword(treebeard, Keyword.UNHASTY));
		assertTrue(scn.HasKeyword(otherGuardian, Keyword.UNHASTY));
		// 2 pipes in play + 1 for each Jetsam discarded by a pipe this phase
		assertEquals(4, scn.FreepsGetChoiceMax());

		scn.FreepsChoose("0");

		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheExtraPipeLastsForThePhaseOfTheDiscardAndNoLonger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jetsam = scn.GetFreepsCard("jetsam1");
		var toby1 = scn.GetFreepsCard("toby1");
		var toby2 = scn.GetFreepsCard("toby2");
		var toby3 = scn.GetFreepsCard("toby3");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gpipe = scn.GetFreepsCard("gpipe");
		var stash = scn.GetFreepsCard("stash");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, gpipe, jetsam, stash);
		scn.MoveCardsToSupportArea(toby1, toby2, toby3);

		scn.StartGame();
		scn.AddBurdens(6);

		// Gandalf's Pipe discards another pipeweed while Jetsam looks on: 1 pipe and no more.
		scn.FreepsUseCardAction(gpipe);
		scn.FreepsChooseCard(toby1);
		assertInZone(Zone.DISCARD, toby1);
		assertAttachedTo(jetsam, gandalf);
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");
		// 6 - 1
		assertEquals(5, scn.GetBurdens());

		scn.FreepsUseCardAction(gpipe);
		scn.FreepsChooseCard(jetsam);
		assertInZone(Zone.DISCARD, jetsam);
		// 1 pipe + 1 for Jetsam
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		// 5 - 2
		assertEquals(3, scn.GetBurdens());

		// Later in the same fellowship phase the extra pipe is still there.
		scn.FreepsUseCardAction(gpipe);
		scn.FreepsChooseCard(toby2);
		assertInZone(Zone.DISCARD, toby2);
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		// 3 - 2
		assertEquals(1, scn.GetBurdens());

		// No minion, so the regroup phase is next. Wizard's Stash lets the pipe be used in it.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsUseCardAction(stash);
		assertInZone(Zone.DISCARD, stash);
		scn.ShadowPass();

		// The last pipeweed in play is the third Old Toby, so it is the one discarded.
		scn.FreepsUseCardAction(gpipe);
		assertInZone(Zone.DISCARD, toby3);
		// 1 pipe: the extra one went with the fellowship phase.
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void DiscardedByAPipeweedThatIsNoPipeItDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jetsam = scn.GetFreepsCard("jetsam1");
		var toby = scn.GetFreepsCard("toby1");
		var longstem = scn.GetFreepsCard("longstem");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gpipe = scn.GetFreepsCard("gpipe");
		var guardian = scn.GetFreepsCard("guardian1");
		scn.MoveCompanionsToTable(gandalf, guardian);
		scn.AttachCardsTo(gandalf, gpipe, jetsam);
		scn.MoveCardsToSupportArea(toby, longstem);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.HasKeyword(guardian, Keyword.UNHASTY));

		scn.FreepsUseCardAction(longstem);
		assertTrue(scn.FreepsHasCardChoiceAvailable(jetsam, toby));
		scn.FreepsChooseCard(jetsam);

		assertInZone(Zone.DISCARD, jetsam);
		// Long-stemmed Pipe was its own only condition to discard.
		assertInZone(Zone.DISCARD, longstem);
		assertTrue(scn.HasKeyword(guardian, Keyword.UNHASTY));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void DiscardedByAPossessionThatIsNoPipeItDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jetsam = scn.GetFreepsCard("jetsam1");
		var toby = scn.GetFreepsCard("toby1");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gpipe = scn.GetFreepsCard("gpipe");
		var apipe = scn.GetFreepsCard("apipe");
		var guardian = scn.GetFreepsCard("guardian1");
		scn.MoveCompanionsToTable(gandalf, aragorn, guardian);
		scn.AttachCardsTo(gandalf, gpipe, jetsam);
		scn.AttachCardsTo(aragorn, apipe);
		scn.MoveCardsToSupportArea(toby);
		scn.MoveCardsToHand(shadowfax);

		scn.StartGame();

		assertTrue(scn.HasKeyword(guardian, Keyword.UNHASTY));
		assertAttachedTo(jetsam, gandalf);

		// Shadowfax goes on Gandalf, the only [gandalf] Wizard, and discards every other possession he bears.
		scn.FreepsPlayCard(shadowfax);

		assertAttachedTo(shadowfax, gandalf);
		assertInZone(Zone.DISCARD, jetsam);
		assertInZone(Zone.DISCARD, gpipe);
		assertTrue(scn.HasKeyword(guardian, Keyword.UNHASTY));

		// Aragorn's Pipe is the one pipe left and Old Toby the one pipeweed: 1 pipe and no more.
		scn.FreepsUseCardAction(apipe);
		assertInZone(Zone.DISCARD, toby);
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("0");

		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheCharacterIsWithoutUnhastyUntilTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jetsam = scn.GetFreepsCard("jetsam1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gpipe = scn.GetFreepsCard("gpipe");
		var guardian = scn.GetFreepsCard("guardian1");
		var otherGuardian = scn.GetFreepsCard("guardian2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, guardian, otherGuardian);
		scn.AttachCardsTo(gandalf, gpipe, jetsam);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		// Jetsam is the only pipeweed, so it is the one discarded.
		scn.FreepsUseCardAction(gpipe);
		assertInZone(Zone.DISCARD, jetsam);
		scn.FreepsChooseCard(guardian);
		// No burden to remove
		scn.FreepsChoose("0");

		assertFalse(scn.HasKeyword(guardian, Keyword.UNHASTY));
		assertTrue(scn.HasKeyword(otherGuardian, Keyword.UNHASTY));

		// In the assignment phase the Free Peoples player may assign him as any companion, and may not assign the other.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.FreepsCanAssign(guardian));
		assertFalse(scn.FreepsCanAssign(otherGuardian));

		scn.SkipToPhase(Phase.REGROUP);
		assertFalse(scn.HasKeyword(guardian, Keyword.UNHASTY));
		assertTrue(scn.HasKeyword(otherGuardian, Keyword.UNHASTY));

		// The turn ends and the other player's begins.
		scn.SkipToOtherPlayersTurn();

		assertTrue(scn.HasKeyword(guardian, Keyword.UNHASTY));
		assertTrue(scn.HasKeyword(otherGuardian, Keyword.UNHASTY));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void UnhastyLostIsNotGivenBackByAnotherCardBeforeTheTurnEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jetsam = scn.GetFreepsCard("jetsam1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gpipe = scn.GetFreepsCard("gpipe");
		var guardian = scn.GetFreepsCard("guardian1");
		var otherGuardian = scn.GetFreepsCard("guardian2");
		scn.MoveCompanionsToTable(gandalf, guardian, otherGuardian);
		scn.AttachCardsTo(gandalf, gpipe, jetsam);

		scn.StartGame();

		scn.FreepsUseCardAction(gpipe);
		scn.FreepsChooseCard(guardian);
		scn.FreepsChoose("0");

		assertFalse(scn.HasKeyword(guardian, Keyword.UNHASTY));
		assertFalse(scn.HasKeyword(gandalf, Keyword.UNHASTY));

		// No card gives a companion unhasty, so a modifier stands in for one.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.COMPANION, null, Keyword.UNHASTY));

		// Gandalf gains it; the Guardian who lost it does not get it back.
		assertTrue(scn.HasKeyword(gandalf, Keyword.UNHASTY));
		assertTrue(scn.HasKeyword(otherGuardian, Keyword.UNHASTY));
		assertFalse(scn.HasKeyword(guardian, Keyword.UNHASTY));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
