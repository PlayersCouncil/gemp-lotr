package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_217_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shapes", "11_217");
					put("shapes2", "11_217");
					// Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul; her text reaches only a companion or ally
					// bearing a [wraith] condition.
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgul, fierce; his text needs 3 burdens.
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] Orc and no Nazgul; strength +4 and fierce only at 2 wounds on the Ring-bearer.
					put("hound", "7_192");
					// Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: a [dwarven] Dwarf companion; a skirmish ability.
					put("gimli", "1_13");
					// Bounder: a [shire] Hobbit ally; a skirmish ability.
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRedhornScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shapes", "11_217");
					put("lemenya", "1_232");    // Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who could be returned to hand
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Redhorn Pass: a Shadows-block mountain, laid by MoveCardToAdventurePath as site 2; "Culture tokens
					// cannot be added, removed, or reinforced."
					put("redhorn", "13_194");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShapesSlowlyAdvancingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Shapes Slowly Advancing
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Regroup:</b> Discard your Nazgûl from play to add a [wraith] token here. The Free Peoples player may return a companion to hand to prevent the placement of that token.
		 * When you can spot 4 [wraith] tokens on this condition, the Ring-bearer is corrupted.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shapes");

		assertEquals("Shapes Slowly Advancing", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupAbilityDiscardsANazgulOfEitherCultureToAddATokenToThisCopyWhenTheFreePeoplesPlayerDeclines() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shapes = scn.GetShadowCard("shapes");
		var shapes2 = scn.GetShadowCard("shapes2");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(shapes);
		scn.MoveCardsToHand(shapes2);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		scn.SetTwilight(5);
		assertInHand(shapes2);

		// The second copy is played from hand in the Shadow phase; it comes with no token.
		scn.FreepsPass();
		scn.ShadowPlayCard(shapes2);
		assertInZone(Zone.SUPPORT, shapes2);
		assertEquals(0, scn.GetCultureTokensOn(shapes2));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(shapes));
		assertEquals(0, scn.GetCultureTokensOn(shapes2));
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya, khamul, hound);
		assertTrue(scn.ShadowActionAvailable(shapes2));

		scn.ShadowUseCardAction(shapes2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		assertInDiscard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, khamul, hound);
		assertTrue(scn.FreepsDecisionAvailable("return a companion to hand"));
		scn.FreepsChooseNo();

		// The token goes on the copy used, "here".
		assertEquals(1, scn.GetCultureTokensOn(shapes2));
		assertEquals(0, scn.GetCultureTokensOn(shapes));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoNazgulToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shapes = scn.GetShadowCard("shapes");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(shapes);

		scn.StartGame();
		scn.MoveMinionsToTable(hound);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(shapes));
	}

	@Test
	public void TheFreePeoplesPlayerReturnsACompanionToHandToPreventTheToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shapes = scn.GetShadowCard("shapes");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToSupportArea(shapes);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);
		// Three tokens: the fourth, prevented, would corrupt the Ring-bearer.
		scn.AddTokensToCard(shapes, 3);
		assertEquals(3, scn.GetCultureTokensOn(shapes));

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(3, scn.GetCultureTokensOn(shapes));
		assertInZone(Zone.FREE_CHARACTERS, aragorn, gimli);
		assertInZone(Zone.SUPPORT, bounder);

		scn.ShadowUseCardAction(shapes);
		assertInDiscard(lemenya);
		assertTrue(scn.FreepsDecisionAvailable("return a companion to hand"));
		scn.FreepsChooseYes();

		// Not the Ring-bearer, who is never returned to hand, and not an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertInHand(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertInZone(Zone.SUPPORT, bounder);
		// The Nazgul discarded stays discarded; no token is placed.
		assertInDiscard(lemenya);
		assertEquals(3, scn.GetCultureTokensOn(shapes));
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoCompanionButTheRingBearerTheFreePeoplesPlayerIsNotAskedAndTheTokenIsPlaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shapes = scn.GetShadowCard("shapes");
		var lemenya = scn.GetShadowCard("lemenya");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToSupportArea(shapes);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(shapes));
		assertInZone(Zone.SUPPORT, bounder);

		scn.ShadowUseCardAction(shapes);
		assertInDiscard(lemenya);
		assertFalse(scn.FreepsDecisionAvailable("return a companion to hand"));
		assertEquals(1, scn.GetCultureTokensOn(shapes));
		assertInZone(Zone.SUPPORT, bounder);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhereNoTokenCanBeAddedTheNazgulIsDiscardedForNothingAndNobodyIsAskedToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRedhornScenario();

		var shapes = scn.GetShadowCard("shapes");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var redhorn = scn.GetShadowSite("redhorn");
		scn.MoveCardsToSupportArea(shapes);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardToAdventurePath(redhorn);
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertSame(redhorn, scn.GetCurrentSite());
		assertEquals(0, scn.GetCultureTokensOn(shapes));
		assertTrue(scn.ShadowActionAvailable(shapes));

		scn.ShadowUseCardAction(shapes);
		assertInDiscard(lemenya);
		assertFalse(scn.FreepsDecisionAvailable("return a companion to hand"));
		assertEquals(0, scn.GetCultureTokensOn(shapes));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FourTokensOnOneCopyCorruptTheRingBearerAndFourBetweenTwoCopiesDoNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shapes = scn.GetShadowCard("shapes");
		var shapes2 = scn.GetShadowCard("shapes2");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToSupportArea(shapes, shapes2);
		scn.AddTokensToCard(shapes, 2);
		scn.AddTokensToCard(shapes2, 1);
		assertEquals(2, scn.GetCultureTokensOn(shapes));
		assertEquals(1, scn.GetCultureTokensOn(shapes2));

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(2, scn.GetCultureTokensOn(shapes));
		assertEquals(1, scn.GetCultureTokensOn(shapes2));

		// Only the Ring-bearer is in the fellowship: nobody is asked to prevent the token.
		scn.ShadowUseCardAction(shapes);
		scn.ShadowChooseCard(lemenya);
		// Three on this copy and one on the other: four [wraith] tokens, none of them four on one condition.
		assertEquals(3, scn.GetCultureTokensOn(shapes));
		assertEquals(1, scn.GetCultureTokensOn(shapes2));
		assertFalse(scn.GameIsFinished());
		assertFalse(scn.FreepsHasLost());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.FreepsPass();
		scn.MarkGameLog();
		scn.ShadowUseCardAction(shapes);
		assertEquals(4, scn.GetCultureTokensOn(shapes));
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasLost());
		assertTrue(scn.GameLogSinceMarkSays(scn.P1() + " lost due to: The Ring-Bearer is corrupted"));
	}
}
