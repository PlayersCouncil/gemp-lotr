package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_18_106_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tale1", "18_106");
					put("tale2", "18_106");
					put("harp", "18_3");        // Thorin's Harp: [dwarven] tale, support-area condition; a maneuver ability used only on purpose
					put("saga", "1_114");       // The Saga of Elendil: [gondor] tale, a condition borne by a [gondor] companion; a skirmish ability only
					put("scroll", "9_36");      // Scroll of Isildur: [gondor] tale, an artifact in the support area; a fellowship ability only
					put("redbook", "2_113");    // Red Book of Westmarch: [shire] tale, a possession in the support area; answers a tale played beside Bilbo
					put("stout", "4_57");       // Stout and Strong: a [dwarven] support-area condition that is no tale; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1
					put("assassin", "2_93");    // Tower Assassin: [sauron] Orc, vitality 3; a maneuver ability used only on purpose
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, vitality 1, so he cannot exert
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli is the Ring-bearer, so that no Frodo is there to be spotted as a Hobbit.
	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tale1", "18_106");
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability only
					put("goldberry", "9_51");   // Goldberry, River-daughter: a [shire] ally who is no Hobbit; her texts need a river
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit companion; +2 only while he bears a weapon
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ADragonsTaleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: A Dragon's Tale
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Tale</b>. To play, spot a Hobbit.<br><b>Maneuver:</b> Discard a tale from play to choose a Shadow player who must exert one of his or her minions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tale1");

		assertEquals("A Dragon's Tale", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAHobbitCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var tale = scn.GetFreepsCard("tale1");
		var smeagol = scn.GetFreepsCard("smeagol");
		var goldberry = scn.GetFreepsCard("goldberry");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveAlliesToTable(goldberry);
		scn.MoveCardsToHand(tale, merry);

		scn.StartGame();

		// A [shire] companion and a [shire] ally, neither a Hobbit, beside a Dwarf Ring-bearer.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(tale));

		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);

		assertTrue(scn.FreepsPlayAvailable(tale));
		scn.FreepsPlayCard(tale);
		assertInZone(Zone.SUPPORT, tale);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHobbitAllyAloneIsEnoughToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var tale = scn.GetFreepsCard("tale1");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(tale);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(tale));
		scn.FreepsPlayCard(tale);
		assertInZone(Zone.SUPPORT, tale);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHobbitWhoIsNotShireAloneIsEnoughToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var tale = scn.GetFreepsCard("tale1");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(tale);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(tale));
		scn.FreepsPlayCard(tale);
		assertInZone(Zone.SUPPORT, tale);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverAbilityDiscardsATaleFromPlayToHaveTheShadowPlayerExertOneOfHisMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale1 = scn.GetFreepsCard("tale1");
		var tale2 = scn.GetFreepsCard("tale2");
		var harp = scn.GetFreepsCard("harp");
		var saga = scn.GetFreepsCard("saga");
		var scroll = scn.GetFreepsCard("scroll");
		var redbook = scn.GetFreepsCard("redbook");
		var stout = scn.GetFreepsCard("stout");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(tale1, harp, scroll, redbook, stout);
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToHand(tale2);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, tale1, harp, scroll, redbook, stout);
		assertInHand(tale2);

		var savage = scn.GetShadowCard("savage");
		var assassin = scn.GetShadowCard("assassin");
		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, assassin, runner1);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(runner1));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(tale1));

		scn.FreepsUseCardAction(tale1);
		// Tales in play of three cultures and four kinds (support-area conditions, a borne condition, an artifact
		// and a possession); not the copy in hand, nor a condition that is no tale.
		assertTrue(scn.FreepsHasCardChoiceAvailable(tale1, harp, saga, scroll, redbook));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(tale2, stout));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(harp);
		assertInDiscard(harp);
		assertInZone(Zone.SUPPORT, tale1);
		assertInHand(tale2);

		// The Shadow player chooses among his minions that can exert: an [isengard] Uruk-hai and a [sauron] Orc,
		// not the Runner (vitality 1), and no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, assassin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner1, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(assassin);

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(runner1));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoMinionAbleToExertTheTaleIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale1 = scn.GetFreepsCard("tale1");
		scn.MoveCardsToSupportArea(tale1);
		assertInZone(Zone.SUPPORT, tale1);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(runner1));
		assertEquals(0, scn.GetWoundsOn(runner2));
		assertTrue(scn.FreepsActionAvailable(tale1));

		// The one tale in play is discarded unasked.
		scn.FreepsUseCardAction(tale1);
		assertInDiscard(tale1);
		assertEquals(0, scn.GetWoundsOn(runner1));
		assertEquals(0, scn.GetWoundsOn(runner2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale1 = scn.GetFreepsCard("tale1");
		var harp = scn.GetFreepsCard("harp");
		scn.MoveCardsToSupportArea(tale1, harp);
		assertInZone(Zone.SUPPORT, tale1, harp);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.ARCHERY);
		// Two tales to pay with, and a minion that could exert.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(tale1));
		assertInZone(Zone.SUPPORT, tale1);
		assertInZone(Zone.SUPPORT, harp);
		assertEquals(0, scn.GetWoundsOn(savage));
	}
}
