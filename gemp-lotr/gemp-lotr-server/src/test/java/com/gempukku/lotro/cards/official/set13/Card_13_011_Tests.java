package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("celeborn", "13_11");
					put("defenses", "1_32");    // Border Defenses: [elven] Skirmish event
					put("elfsong", "1_39");     // Elf-song: [elven] Fellowship event
					put("shake", "8_13");       // Shake Off the Shadow: an [elven] condition, no event
					put("sentinels", "2_37");   // Sentinels of Númenor: a [gondor] Skirmish event
					put("lightfoot", "1_52");   // Lightfootedness: an [elven] Archery event, here in the Shadow player's discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: Eastemnet Gullies at site 1 is no forest, Derndingle at site 2 is a forest that is no sanctuary.
	protected VirtualTableScenario GetDerndingleScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("celeborn", "13_11");
					put("defender", "18_9");    // Elven Defender: Elf companion, resistance 7; +2 strength only while an [elven] token is spotted
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion, resistance 6
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_330");      // Derndingle: Forest. Move limit +1 while an unbound Hobbit is there; none is
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Rivendell Waterfall as site 3 (Forest, Sanctuary), with Aragorn, a ranger: the move limit is 3 there, so a turn
	// has two regroup moves.
	protected VirtualTableScenario GetWaterfallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("celeborn", "13_11");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger; a maneuver ability used only on purpose
					put("defenses", "1_32");    // Border Defenses: [elven] Skirmish event
					put("elfsong", "1_39");     // Elf-song: [elven] Fellowship event
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_342");      // Rivendell Waterfall
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CelebornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Celeborn, The Wise
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: While the fellowship is at a forest site, each Elf who has resistance 7 or more gains <b>muster</b>.<br>Each time the fellowship moves during the regroup phase, you may take an [elven] event into hand from your discard pile.
		*/

		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");

		assertEquals("Celeborn", celeborn.getBlueprint().getTitle());
		assertEquals("The Wise", celeborn.getBlueprint().getSubtitle());
		assertTrue(celeborn.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, celeborn.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, celeborn.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, celeborn.getBlueprint().getCardType());
		assertEquals(Race.ELF, celeborn.getBlueprint().getRace());
		assertEquals(2, celeborn.getBlueprint().getTwilightCost());
		assertEquals(6, celeborn.getBlueprint().getStrength());
		assertEquals(3, celeborn.getBlueprint().getVitality());
		assertEquals(7, celeborn.getBlueprint().getResistance());
	}

	@Test
	public void AtDerndingleEachElfOfResistance7GainsMusterAndLosesItAt6() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDerndingleScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var defender = scn.GetFreepsCard("defender");
		var haldir = scn.GetFreepsCard("haldir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(celeborn, defender, haldir);

		scn.StartGame();

		// Eastemnet Gullies is no forest.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(celeborn, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(defender, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(haldir, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));

		scn.FreepsPass();

		// At Derndingle: the two Elves of resistance 7, Celeborn himself among them; not Haldir (an Elf of 6), nor
		// Frodo (10, and no Elf).
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetResistance(celeborn));
		assertEquals(7, scn.GetResistance(defender));
		assertEquals(6, scn.GetResistance(haldir));
		assertEquals(10, scn.GetResistance(frodo));
		assertTrue(scn.HasKeyword(celeborn, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(defender, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(haldir, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));

		// A burden takes every companion's resistance down by 1: at 6 neither keeps it.
		scn.AddBurdens(1);
		assertEquals(6, scn.GetResistance(celeborn));
		assertEquals(6, scn.GetResistance(defender));
		assertFalse(scn.HasKeyword(celeborn, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(defender, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtRivendellWaterfallHeHasMusterUntilTheFellowshipMovesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWaterfallScenario();

		var celeborn = scn.GetFreepsCard("celeborn");

		scn.StartGame();
		// Laid after the skip: with muster at the Waterfall he would stop it at the regroup phase's muster offer.
		scn.SkipToSite(3);
		scn.MoveCompanionsToTable(celeborn);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(celeborn, Keyword.MUSTER));

		// On to Balin's Tomb, which is no forest.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(celeborn, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARegroupMoveOffersAnElvenEventFromTheDiscardPileAndTheFellowshipPhaseMoveDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var defenses = scn.GetFreepsCard("defenses");
		var elfsong = scn.GetFreepsCard("elfsong");
		var shake = scn.GetFreepsCard("shake");
		var sentinels = scn.GetFreepsCard("sentinels");
		var lightfoot = scn.GetShadowCard("lightfoot");
		scn.MoveCompanionsToTable(celeborn);
		scn.MoveCardsToDiscard(defenses, elfsong, shake, sentinels, lightfoot);

		scn.StartGame();
		assertInDiscard(defenses, elfsong, shake, sentinels, lightfoot);

		// The move that ends the fellowship phase is no regroup move.
		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(celeborn));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInDiscard(defenses, elfsong);

		scn.SkipToMovementDecision();
		int hand = scn.GetFreepsHandCount();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(celeborn));
		scn.FreepsAcceptOptionalTrigger();

		// Either [elven] event of his own discard pile; not the [elven] condition, the [gondor] event, or the
		// Shadow player's [elven] event in his discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(defenses, elfsong));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shake, sentinels, lightfoot));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(elfsong);

		assertInHand(elfsong);
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertInDiscard(defenses, shake, sentinels, lightfoot);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesTheDiscardPileAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var defenses = scn.GetFreepsCard("defenses");
		scn.MoveCompanionsToTable(celeborn);
		scn.MoveCardsToDiscard(defenses);

		scn.StartGame();
		assertInDiscard(defenses);

		scn.SkipToMovementDecision();
		int hand = scn.GetFreepsHandCount();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(celeborn));
		scn.FreepsDeclineOptionalTrigger();

		assertInDiscard(defenses);
		assertEquals(hand, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoElvenEventInTheDiscardPileARegroupMoveOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var shake = scn.GetFreepsCard("shake");
		var sentinels = scn.GetFreepsCard("sentinels");
		scn.MoveCompanionsToTable(celeborn);
		scn.MoveCardsToDiscard(shake, sentinels);

		scn.StartGame();
		assertInDiscard(shake, sentinels);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(celeborn));
		assertInDiscard(shake, sentinels);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachOfTwoRegroupMovesInOneTurnOffersAnElvenEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWaterfallScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defenses = scn.GetFreepsCard("defenses");
		var elfsong = scn.GetFreepsCard("elfsong");
		scn.MoveCompanionsToTable(celeborn, aragorn);
		scn.MoveCardsToDiscard(defenses, elfsong);

		scn.StartGame();
		assertInDiscard(defenses, elfsong);

		scn.FreepsPass();
		scn.SkipToMovementDecision();

		// The first regroup move, to Rivendell Waterfall.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(celeborn));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCards(defenses);
		assertInHand(defenses);
		assertInDiscard(elfsong);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetMoveLimit());

		// The Waterfall is a forest: Celeborn has muster there, and it is offered as the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Use Muster"));
		scn.FreepsDeclineOptionalTrigger();
		scn.SkipToMovementDecision();

		// The second regroup move of the same turn.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(celeborn));
		scn.FreepsAcceptOptionalTrigger();
		assertInHand(elfsong);
		assertEquals(3, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
