package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_108_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leaf", "17_108");
					put("halfling", "17_106");  // Halfling Leaf: [shire] pipeweed condition; a skirmish ability used only on purpose
					put("ship", "18_27");       // Ship of Smoke: [gandalf] pipeweed condition; its trigger needs Gandalf, never in play
					put("toby", "1_305");       // Old Toby: [shire] pipeweed possession in the support area
					put("toby2", "1_305");      // a second Old Toby, kept in hand
					put("pipe", "1_285");       // Bilbo's Pipe: a [shire] pipe on a Hobbit, which is no pipeweed

					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] unbound Hobbit; his text is about tales
					// Merry, Master Holbytla: a [rohan] unbound Hobbit; his abilities are used only on purpose
					put("merry", "103_85");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion who is a Man

					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai minion, damage +1
					put("armory", "1_157");     // Uruk-hai Armory: a Shadow condition, no minion; archery total only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HornblowerLeafStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Hornblower Leaf
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Pipeweed</b>. To play, spot an unbound Hobbit.<br><b>Regroup:</b> Discard a pipeweed from play to remove a threat.<br><b>Regroup:</b> Discard a pipeweed from play to discard a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leaf");

		assertEquals("Hornblower Leaf", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnUnboundHobbitIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(leaf, pippin);

		scn.StartGame();
		assertInHand(leaf, pippin);
		// The Ring-bearer and Sam are Hobbits but Ring-bound, Bounder is a Hobbit but no companion, and Aragorn is
		// unbound but no Hobbit.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(leaf));

		scn.FreepsPlayCard(pippin);
		assertTrue(scn.FreepsPlayAvailable(leaf));

		scn.FreepsPlayCard(leaf);
		assertInZone(Zone.SUPPORT, leaf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MayBePlayedWithAnUnboundHobbitOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(leaf);

		scn.StartGame();
		assertInHand(leaf);
		assertTrue(scn.FreepsPlayAvailable(leaf));

		scn.FreepsPlayCard(leaf);
		assertInZone(Zone.SUPPORT, leaf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionDiscardsAPipeweedOfAnyKindFromPlayToRemoveAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var halfling = scn.GetFreepsCard("halfling");
		var ship = scn.GetFreepsCard("ship");
		var toby = scn.GetFreepsCard("toby");
		var toby2 = scn.GetFreepsCard("toby2");
		var pipe = scn.GetFreepsCard("pipe");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.AttachCardsTo(pippin, pipe);
		scn.MoveCardsToSupportArea(leaf, halfling, ship, toby);
		scn.MoveCardsToHand(toby2);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// Three companions hold up to three threats.
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SUPPORT, leaf, halfling, ship, toby);
		assertInZone(Zone.ATTACHED, pipe);
		assertInHand(toby2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(leaf));

		scn.FreepsUseCardAction(leaf, "Remove a threat");
		// Any pipeweed in play, this one included, of either culture and either type; not the pipe, nor the
		// pipeweed in hand.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(leaf, halfling, ship, toby));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pipe, toby2));

		scn.FreepsChooseCard(ship);
		assertInZone(Zone.DISCARD, ship);
		assertInZone(Zone.SUPPORT, leaf, halfling, toby);
		assertInZone(Zone.ATTACHED, pipe);
		assertInHand(toby2);
		// One threat of the two.
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionDiscardsAPipeweedOfAnyKindFromPlayToDiscardAMinionOfAnyKind() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var halfling = scn.GetFreepsCard("halfling");
		var ship = scn.GetFreepsCard("ship");
		var toby = scn.GetFreepsCard("toby");
		var toby2 = scn.GetFreepsCard("toby2");
		var pipe = scn.GetFreepsCard("pipe");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.AttachCardsTo(pippin, pipe);
		scn.MoveCardsToSupportArea(leaf, halfling, ship, toby);
		scn.MoveCardsToHand(toby2);
		var brigand = scn.GetShadowCard("brigand");
		var party = scn.GetShadowCard("party");
		var armory = scn.GetShadowCard("armory");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// Minions that lived through the skirmishes stand here.
		scn.MoveMinionsToTable(brigand, party);
		scn.MoveCardsToSupportArea(armory);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, brigand, party);
		assertInZone(Zone.SUPPORT, armory, leaf, halfling, ship, toby);
		assertInZone(Zone.ATTACHED, pipe);
		assertInHand(toby2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.FreepsUseCardAction(leaf, "Discard a minion");
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(leaf, halfling, ship, toby));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pipe, toby2));
		scn.FreepsChooseCard(toby);
		assertInZone(Zone.DISCARD, toby);
		assertInZone(Zone.SUPPORT, leaf, halfling, ship);

		// A minion of either culture and race; not a Shadow card that is no minion, nor a companion.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(brigand, party));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armory, pippin, aragorn, frodo));
		scn.FreepsChooseCard(party);
		assertInZone(Zone.DISCARD, party);
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertInZone(Zone.SUPPORT, armory);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoThreatAndNoMinionBothActionsAreOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var halfling = scn.GetFreepsCard("halfling");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(leaf, halfling);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertInZone(Zone.SUPPORT, leaf, halfling);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(leaf));

		scn.FreepsUseCardAction(leaf, "Discard a minion");
		scn.FreepsChooseCard(halfling);
		assertInZone(Zone.DISCARD, halfling);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(leaf));
		// The Leaf alone is left to pay with, and pays for itself.
		scn.FreepsUseCardAction(leaf, "Remove a threat");
		assertInZone(Zone.DISCARD, leaf);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionsAreNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var halfling = scn.GetFreepsCard("halfling");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveCardsToSupportArea(leaf, halfling);
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertInZone(Zone.SUPPORT, leaf, halfling);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(leaf));
	}
}
