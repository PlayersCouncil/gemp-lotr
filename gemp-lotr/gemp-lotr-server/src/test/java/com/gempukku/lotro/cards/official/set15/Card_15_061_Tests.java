package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("prowler", "15_61");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight is his whole text
					// Gondorian Merchant: the [gondor] Man ally, vitality 1; a Maneuver ability used only on purpose
					put("merchant", "7_97");
					// Narsil, Blade of the Faithful: on a [gondor] Man, vitality +1, so that the Merchant can exert; its
					// Response answers a wound to its bearer only
					put("narsil", "9_34");
					// Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit); a Skirmish ability used only on purpose
					put("pippin", "103_31");
					// Elite Rider: a Man companion of another culture ([rohan]); +2 only while skirmishing a wounded minion
					put("rider", "4_265");
					// A Path Appointed: [gondor] support-area condition that takes [gondor] tokens by its own Fellowship
					// ability; a Regroup ability used only on purpose
					put("path", "8_42");
					// Stout and Strong: [dwarven] support-area condition that takes [dwarven] tokens as a Dwarf wins; a
					// Skirmish ability used only on purpose
					put("stout", "4_57");

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1, site 4; its text is about being played
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, site 5; Damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The only companions in the deck are the two Prowlers, Aragorn, the Ranger of Osgiliath and Merry, so that the
	// starting fellowship offers no one else.
	protected VirtualTableScenario GetStartingFellowshipScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("prowler", "15_61");
					put("prowler2", "15_61");
					// Aragorn, Ranger of the North: a [gondor] Man ranger of cost 4; a Maneuver ability only
					put("aragorn", "1_89");
					// Ranger of Osgiliath: a [gondor] Man ranger of cost 2; a Regroup ability only
					put("osgiliath", "7_116");
					// Merry, Friend to Sam: a companion of cost 1; a Skirmish ability used only on purpose
					put("merry", "1_302");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorianProwlerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Gondorian Prowler
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ranger</b>. <b>Hunter 1</b>.
		 * 	When this companion is in your starting fellowship, he is twilight cost -1.
		 * 	<b>Skirmish:</b> Remove a [gondor] token or exert a [gondor] Man to make the site number of a minion +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("prowler");

		assertEquals("Gondorian Prowler", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void InTheStartingFellowshipEachProwlerCostsOneAndOtherGondorRangersCostTheirOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStartingFellowshipScenario();

		var prowler = scn.GetFreepsCard("prowler");
		var prowler2 = scn.GetFreepsCard("prowler2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		var merry = scn.GetFreepsCard("merry");

		// The game has not been started: player 1 is choosing his starting fellowship. Aragorn at 4 fits alone.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(prowler, prowler2, aragorn, osgiliath, merry));

		scn.FreepsChooseCard(prowler);
		// 2 - 1 of the 4 a starting fellowship may cost. Aragorn, a [gondor] ranger at his printed 4, no longer fits:
		// he would at 3, had the discount been every [gondor] ranger's, Man's or companion's.
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(prowler2, osgiliath, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));

		scn.FreepsChooseCard(prowler2);
		// 1 + 1: the second Prowler's own -1, neither lost nor doubled by the first's.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(osgiliath, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));

		scn.FreepsChooseCard(osgiliath);
		// 2 + 2 = 4: not even Merry fits, nobody is left to offer, and player 1 is asked only to press DONE. Had
		// the Ranger of Osgiliath cost 1, Merry would still be offered.
		assertInZone(Zone.FREE_CHARACTERS, prowler, prowler2, osgiliath);
		assertInZone(Zone.DECK, aragorn, merry);
		assertTrue(scn.FreepsIsAskedOnlyToConfirm());

		scn.FreepsConfirm();

		// The fellowship is complete, and player 2 is asked.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void PlayedFromHandInTheFellowshipPhaseHeCostsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prowler = scn.GetFreepsCard("prowler");
		scn.MoveCardsToHand(prowler);

		scn.StartGame();
		assertInHand(prowler);
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetTwilightCost(prowler));

		scn.FreepsPlayCard(prowler);
		assertInZone(Zone.FREE_CHARACTERS, prowler);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingAGondorManMakesTheSiteNumberOfAMinionPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prowler = scn.GetFreepsCard("prowler");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(prowler, knight, pippin, rider);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);

		scn.StartGame();
		assertEquals(2, scn.GetVitality(merchant));
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{prowler, runner}, new PhysicalCardImpl[]{knight, savage});
		scn.FreepsResolveSkirmish(prowler);

		// No [gondor] token anywhere: the exertion is the only cost that can be paid, and nothing asks which.
		assertEquals(4, scn.GetMinionSiteNumber(runner));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsActionAvailable(prowler));
		scn.FreepsUseCardAction(prowler);

		// A [gondor] Man, companion or ally, the Prowler himself among them; not a [gondor] companion who is no Man,
		// nor a Man of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(prowler, knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, rider));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(prowler));

		// A minion in this skirmish or out of it, of either culture and race; not a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(prowler, knight));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);
		assertEquals(7, scn.GetMinionSiteNumber(savage));
		assertEquals(4, scn.GetMinionSiteNumber(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still so at the next decision of the same skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetMinionSiteNumber(savage));

		// Each skirmish is a phase of its own: in the Savage's own skirmish it is back to 5.
		scn.FreepsPass();
		assertInDiscard(runner);
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetMinionSiteNumber(savage));
	}

	@Test
	public void WithEveryGondorManExhaustedAGondorTokenIsRemovedAndADwarvenTokenIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prowler = scn.GetFreepsCard("prowler");
		var path = scn.GetFreepsCard("path");
		var stout = scn.GetFreepsCard("stout");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(prowler);
		scn.MoveCardsToSupportArea(path, stout);

		scn.StartGame();
		scn.AddTokensToCard(path, 1);
		scn.AddTokensToCard(stout, 1);
		// Vitality 3 with 2 wounds: the only [gondor] Man cannot exert.
		scn.AddWoundsToChar(prowler, 2);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(2, scn.GetWoundsOn(prowler));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(prowler, runner);
		scn.FreepsResolveSkirmish(prowler);

		assertEquals(4, scn.GetMinionSiteNumber(runner));
		assertTrue(scn.FreepsActionAvailable(prowler));
		scn.FreepsUseCardAction(prowler);

		// The [gondor] token goes, with nothing asked; the [dwarven] token stays. The only minion takes the +2.
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(2, scn.GetWoundsOn(prowler));
		assertEquals(6, scn.GetMinionSiteNumber(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithBothCostsPayableThePlayerIsAskedWhich() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prowler = scn.GetFreepsCard("prowler");
		var path = scn.GetFreepsCard("path");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(prowler);
		scn.MoveCardsToSupportArea(path);

		scn.StartGame();
		scn.AddTokensToCard(path, 1);
		assertEquals(1, scn.GetCultureTokensOn(path));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(prowler, runner);
		scn.FreepsResolveSkirmish(prowler);

		assertEquals(0, scn.GetWoundsOn(prowler));
		assertEquals(4, scn.GetMinionSiteNumber(runner));
		scn.FreepsUseCardAction(prowler);

		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("token"));
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		scn.FreepsChooseOption("Exert");

		// He is the only [gondor] Man, and exerts with nothing more asked; the token stays.
		assertEquals(1, scn.GetWoundsOn(prowler));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(6, scn.GetMinionSiteNumber(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithNoGondorTokenAndEveryGondorManExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prowler = scn.GetFreepsCard("prowler");
		var path = scn.GetFreepsCard("path");
		var stout = scn.GetFreepsCard("stout");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(prowler);
		scn.MoveCardsToSupportArea(path, stout);

		scn.StartGame();
		scn.AddTokensToCard(stout, 1);
		scn.AddWoundsToChar(prowler, 2);
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(2, scn.GetWoundsOn(prowler));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(prowler, runner);
		scn.FreepsResolveSkirmish(prowler);

		// A [dwarven] token pays nothing.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(prowler));

		// With a [gondor] token on the Path, it is.
		scn.AddTokensToCard(path, 1);
		assertEquals(1, scn.GetCultureTokensOn(path));
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(prowler));
	}
}
