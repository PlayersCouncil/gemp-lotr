package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_107_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pipe", "3_107");
					put("legolas", "1_50");     // Legolas, Greenleaf: an [elven] Elf companion, Frodo signet, vitality 3; an archery ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion, Frodo signet, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion of the Gandalf signet
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion of the Aragorn signet; his abilities are used only on purpose
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: a companion with no signet (to play, spot an Elf: cheated in)
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, no signet; a skirmish ability only

					// Bilbo's Pipe: a [shire] pipe, on a Hobbit; Aragorn's Pipe: a [gondor] pipe, on a [gondor] companion.
					// Their abilities are used only on purpose.
					put("bilbospipe", "1_285");
					put("aragornspipe", "1_91");
					// Old Toby: a [shire] pipeweed possession in the support area; its play offers a draw
					put("oldtoby", "1_305");
					// Wizard's Stash: a [gandalf] pipeweed possession borne by a Wizard; each companion bearing a pipe is
					// strength +1, which no test reads
					put("stash", "103_7");
					// Halfling Leaf: a [shire] pipeweed that is a condition, not a possession
					put("halfling", "17_106");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: Frodo signet, vitality 4
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGaladrielRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pipe", "3_107");
					// Frodo, Frenzied Fighter: the one Frodo who cannot bear the Ring, so the one who may be in play beside
					// another Ring-bearer (Q-0159); cost 2, and his texts want more minions than companions, or a skirmish
					put("frodo", "13_149");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit companion who is not Frodo
				}},
				VirtualTableScenario.FellowshipSites,
				// Galadriel, Bearer of Wisdom: her only text changes her own resistance
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodosPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Frodo's Pipe
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Pipe
		 * Game Text: Bearer must be Frodo.<br><b>Fellowship:</b> Discard a pipeweed possession and spot X pipes to heal a companion with the Frodo signet X times.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pipe");

		assertEquals("Frodo's Pipe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PIPE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnFrodoTheRingBearerAndNotOnSam() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(pipe);

		scn.StartGame();

		assertInZone(Zone.HAND, pipe);
		assertTrue(scn.FreepsPlayAvailable(pipe));

		// Frodo is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(pipe);
		assertAttachedTo(pipe, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysOnAFrodoWhoIsNotTheRingBearerAndNotOnTheRingBearerOrSam() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var frodo = scn.GetFreepsCard("frodo");
		var sam = scn.GetFreepsCard("sam");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(frodo, pipe);

		scn.StartGame();

		// Galadriel the Ring-bearer and Sam, a Ring-bound Hobbit, and no Frodo: nobody may bear it.
		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());
		assertInZone(Zone.HAND, pipe);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(pipe));

		scn.FreepsPlayCard(frodo);
		assertTrue(scn.FreepsPlayAvailable(pipe));

		// Frodo is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(pipe);
		assertAttachedTo(pipe, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardingAPipeweedPossessionAndSpottingTwoPipesHealsOneFrodoSignetCompanionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var haldir = scn.GetFreepsCard("haldir");
		var bounder = scn.GetFreepsCard("bounder");
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var oldtoby = scn.GetFreepsCard("oldtoby");
		var stash = scn.GetFreepsCard("stash");
		var halfling = scn.GetFreepsCard("halfling");
		scn.MoveCompanionsToTable(legolas, gandalf, aragorn, sam, haldir);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(frodo, pipe);
		scn.AttachCardsTo(sam, bilbospipe);
		scn.AttachCardsTo(aragorn, aragornspipe);
		scn.AttachCardsTo(gandalf, stash);
		scn.MoveCardsToSupportArea(oldtoby, halfling);

		scn.StartGame();
		// Gandalf carries three wounds, so that two heals read 1; everyone else who could be chosen is wounded too.
		scn.AddWoundsToChar(gandalf, 3);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(haldir, 1);
		scn.AddWoundsToChar(bounder, 1);
		scn.FreepsRefreshDecision();

		assertAttachedTo(pipe, frodo);
		assertAttachedTo(bilbospipe, sam);
		assertAttachedTo(aragornspipe, aragorn);
		assertAttachedTo(stash, gandalf);
		assertInZone(Zone.SUPPORT, oldtoby, halfling);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.FreepsActionAvailable(pipe));

		scn.FreepsUseCardAction(pipe);

		// Pipeweed possessions of two cultures, one in the support area and one borne, are offered; a pipeweed
		// condition and possessions that are no pipeweed are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(oldtoby, stash));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(halfling, pipe, bilbospipe, aragornspipe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(stash);
		assertInZone(Zone.DISCARD, stash);
		assertInZone(Zone.SUPPORT, oldtoby, halfling);

		// Three pipes, of two cultures, this one among them; Old Toby and Halfling Leaf are no pipes.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");

		// One companion of the Frodo signet, of whatever race and culture; another signet, none, or an ally is not
		// offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, legolas, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, sam, haldir, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(gandalf);

		// Healed twice: 3 wounds less 2.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(bounder));
		// The pipes are spotted, not discarded.
		assertAttachedTo(pipe, frodo);
		assertAttachedTo(bilbospipe, sam);
		assertAttachedTo(aragornspipe, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOnlyItselfToSpotItHealsTheCompanionOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var oldtoby = scn.GetFreepsCard("oldtoby");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(frodo, pipe);
		scn.MoveCardsToSupportArea(oldtoby);

		scn.StartGame();
		scn.AddWoundsToChar(legolas, 2);
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();

		assertAttachedTo(pipe, frodo);
		assertInZone(Zone.SUPPORT, oldtoby);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(pipe));

		scn.FreepsUseCardAction(pipe);
		// Old Toby, the one pipeweed possession, goes without a question.
		assertInZone(Zone.DISCARD, oldtoby);

		// The pipe spots itself.
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, legolas));
		scn.FreepsChooseCard(legolas);

		// Healed once: 2 wounds less 1.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void XOfZeroMayBeChosenAndThePipeweedIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var oldtoby = scn.GetFreepsCard("oldtoby");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(frodo, pipe);
		scn.MoveCardsToSupportArea(oldtoby);

		scn.StartGame();
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, oldtoby);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(pipe));

		scn.FreepsUseCardAction(pipe);
		assertInZone(Zone.DISCARD, oldtoby);

		// The pipeweed is paid whatever X is, so X of 0 is a use like another (Gandalf's Pipe 1_74).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChoose("0");
		// The companion is still chosen, and is healed no times.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, legolas));
		scn.FreepsChooseCard(legolas);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoCompanionOfTheFrodoSignetWoundedItIsStillOfferedAndThePipeweedIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var oldtoby = scn.GetFreepsCard("oldtoby");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.AttachCardsTo(frodo, pipe);
		scn.MoveCardsToSupportArea(oldtoby);

		scn.StartGame();
		// The only wounded companion is Aragorn, of the Gandalf signet.
		scn.AddWoundsToChar(aragorn, 1);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, oldtoby);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// The ability has a cost, so it is offered though there is nobody to heal (Q-0034).
		assertTrue(scn.FreepsActionAvailable(pipe));

		scn.FreepsUseCardAction(pipe);
		assertInZone(Zone.DISCARD, oldtoby);
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		// Nobody of the Frodo signet is wounded, so nobody is asked about and nobody is healed.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedUntilAPipeweedPossessionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var halfling = scn.GetFreepsCard("halfling");
		var oldtoby = scn.GetFreepsCard("oldtoby");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.AttachCardsTo(frodo, pipe);
		scn.AttachCardsTo(aragorn, aragornspipe);
		scn.MoveCardsToSupportArea(halfling);
		scn.MoveCardsToHand(oldtoby);

		scn.StartGame();
		scn.AddWoundsToChar(legolas, 1);
		scn.FreepsRefreshDecision();

		// A pipeweed condition, two possessions that are no pipeweed, and a wounded companion of the Frodo signet.
		assertInZone(Zone.SUPPORT, halfling);
		assertInZone(Zone.HAND, oldtoby);
		assertAttachedTo(pipe, frodo);
		assertAttachedTo(aragornspipe, aragorn);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pipe));

		// A pipeweed possession played by legal play.
		scn.FreepsPlayCard(oldtoby);
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, oldtoby);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(pipe));
	}
}
