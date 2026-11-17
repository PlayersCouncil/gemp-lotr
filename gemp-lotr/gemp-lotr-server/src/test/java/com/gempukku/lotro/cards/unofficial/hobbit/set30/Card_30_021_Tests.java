package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_021_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elrond", "30_21");
					put("radagast", "31_17");   // Radagast, The Brown: [gandalf] Wizard ally, Wise; a fellowship ability only
					put("thranduil", "31_12");  // Thranduil, Elven King: [elven] Elf ally, not Wise; a maneuver ability only
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion, Wise; a fellowship ability, a discard bar
					// that needs 2 [dwarven] followers, and a discount on Thorin
					put("gandalf", "30_26");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion; a fellowship ability only
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, vitality 2; an assignment ability only
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ElrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Elrond, Elven Lord
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Ally
		 * Subtype: Elf
		 * Home: 3H
		 * Strength: 8
		 * Vitality: 4
		 * Game Text: <b>Wise</b>.<br>At the start of each of your turns, you may heal a Wise ally (or heal Elrond twice).<br><b>Regroup:</b> Exert Elrond twice to heal a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elrond");

		assertEquals("Elrond", card.getBlueprint().getTitle());
		assertEquals("Elven Lord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.WISE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 3)));
	}

	@Test
	public void AtTheStartOfHisOwnersTurnElrondWithTwoWoundsIsHealedTwiceUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveAlliesToTable(elrond, radagast);
		scn.AddWoundsToChar(elrond, 2);
		scn.AddWoundsToChar(radagast, 1);
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(radagast));

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		scn.FreepsAcceptOptionalTrigger();

		// The parenthesis can be done in full, so it is what happens: no choice, and Radagast keeps his wound.
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfHisOwnersTurnElrondWithThreeWoundsIsHealedTwiceAndNoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveAlliesToTable(elrond, radagast);
		scn.AddWoundsToChar(elrond, 3);
		scn.AddWoundsToChar(radagast, 1);
		assertEquals(3, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(radagast));

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));
		scn.FreepsAcceptOptionalTrigger();

		// Twice: one of his three wounds is left.
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithElrondAtOneWoundTheHealGoesToAWiseAllyOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var radagast = scn.GetFreepsCard("radagast");
		var thranduil = scn.GetFreepsCard("thranduil");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveAlliesToTable(elrond, radagast, thranduil);
		scn.MoveCompanionsToTable(gandalf);
		scn.AddWoundsToChar(elrond, 1);
		scn.AddWoundsToChar(radagast, 1);
		scn.AddWoundsToChar(thranduil, 1);
		scn.AddWoundsToChar(gandalf, 1);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertEquals(1, scn.GetWoundsOn(thranduil));
		assertEquals(1, scn.GetWoundsOn(gandalf));

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));
		scn.FreepsAcceptOptionalTrigger();

		// Wise allies of two cultures and races, Elrond among them; not an ally who is not Wise, nor a Wise companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elrond, radagast));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thranduil, gandalf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(radagast);

		assertEquals(0, scn.GetWoundsOn(radagast));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(thranduil));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoWiseAllyWoundedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var radagast = scn.GetFreepsCard("radagast");
		var thranduil = scn.GetFreepsCard("thranduil");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveAlliesToTable(elrond, radagast, thranduil);
		scn.MoveCompanionsToTable(gandalf);
		// Wounded, but neither is a Wise ally.
		scn.AddWoundsToChar(thranduil, 1);
		scn.AddWoundsToChar(gandalf, 1);
		assertEquals(1, scn.GetWoundsOn(thranduil));
		assertEquals(1, scn.GetWoundsOn(gandalf));

		scn.StartGame();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(elrond));
		assertEquals(1, scn.GetWoundsOn(thranduil));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningTheOfferHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveAlliesToTable(elrond, radagast);
		scn.AddWoundsToChar(elrond, 1);
		scn.AddWoundsToChar(radagast, 1);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(radagast));

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOfferComesAgainAtHisOwnersNextTurnAndNotAtTheOtherPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveAlliesToTable(elrond, radagast);
		scn.AddWoundsToChar(radagast, 1);
		assertEquals(1, scn.GetWoundsOn(radagast));

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Turn 2, the other player's: nothing is offered to Elrond's owner.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(elrond));
		assertEquals(1, scn.GetWoundsOn(radagast));

		// Turn 3, his owner's: offered again, and taken. Radagast is the one Wise ally wounded, healed unasked.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(radagast));
		// The Troll Hoard (site 2) makes no offer of its own: no hand weapon in the deck (F-0800).
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
	}

	@Test
	public void RegroupActionExertsElrondTwiceToHealACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var radagast = scn.GetFreepsCard("radagast");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		// Put down after the start of the turn, so that his own start-of-turn heal is not asked.
		scn.MoveAlliesToTable(elrond, radagast);
		scn.MoveCompanionsToTable(balin);
		scn.MoveMinionsToTable(aggressor);
		scn.AddWoundsToChar(bilbo, 2);
		scn.AddWoundsToChar(balin, 1);
		scn.AddWoundsToChar(radagast, 1);
		scn.AddWoundsToChar(aggressor, 1);
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(elrond));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsActionAvailable(elrond));
		scn.FreepsUseCardAction(elrond);
		assertEquals(2, scn.GetWoundsOn(elrond));

		// Companions of two cultures and races, the Ring-bearer among them; not an ally, nor a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(radagast, elrond, aggressor));
		scn.FreepsChooseCard(bilbo);

		// One wound healed of two.
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoCompanionWoundedTheRegroupActionIsOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var radagast = scn.GetFreepsCard("radagast");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveAlliesToTable(elrond, radagast);
		scn.AddWoundsToChar(radagast, 1);
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsActionAvailable(elrond));
		scn.FreepsUseCardAction(elrond);

		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhileElrondCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		// Vitality 4 with 2 wounds: he can exert once, not twice.
		scn.AddWoundsToChar(elrond, 2);
		scn.AddWoundsToChar(bilbo, 1);
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(bilbo));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(elrond));
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(bilbo));
	}
}
