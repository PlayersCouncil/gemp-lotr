package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_07_218_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("otsea", "7_218");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound [shire] Hobbit who is not the Ring-bearer, vitality 4
					put("smeagol", "7_72");     // Sméagol, Hurried Guide: a Ring-bound [gollum] companion who is no Hobbit, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man companion
					put("legolas", "1_50");     // Legolas, Greenleaf: an unbound [elven] Elf companion
					put("herdsman", "7_247");   // Rohirrim Herdsman: a Man ally (only companions are unbound); a regroup ability used only on purpose
					// Cards for the Free Peoples hand, which decides who has initiative.
					put("hand1", "1_13");
					put("hand2", "1_13");
					put("hand3", "1_13");
					put("hand4", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireOtseaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Úlairë Otsëa, Black-Mantled Wraith
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Regroup:</b> If you have initiative, discard Úlairë Otsëa to make the Free Peoples player exert a Ring-bound companion twice or return an unbound companion to his or her hand.
		*/

		var scn = GetScenario();

		var otsea = scn.GetFreepsCard("otsea");

		assertEquals("Úlairë Otsëa", otsea.getBlueprint().getTitle());
		assertEquals("Black-Mantled Wraith", otsea.getBlueprint().getSubtitle());
		assertTrue(otsea.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, otsea.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, otsea.getBlueprint().getCulture());
		assertEquals(CardType.MINION, otsea.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, otsea.getBlueprint().getRace());
		assertEquals(4, otsea.getBlueprint().getTwilightCost());
		assertEquals(9, otsea.getBlueprint().getStrength());
		assertEquals(3, otsea.getBlueprint().getVitality());
		assertEquals(3, otsea.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithInitiativeRegroupAbilityDiscardsHerAndTheFreePeoplesPlayerChoosesToExertARingBoundCompanionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(sam, smeagol, aragorn, legolas);
		scn.MoveAlliesToTable(herdsman);
		// Vitality 4 with 2 wounds: Sam can exert once, not twice.
		scn.AddWoundsToChar(sam, 2);
		assertEquals(2, scn.GetWoundsOn(sam));

		scn.StartGame();
		// No minion in the Shadow phase: straight on to the regroup phase.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(otsea);
		// Three cards in the Free Peoples hand: the Shadow has initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowActionAvailable(otsea));

		scn.ShadowUseCardAction(otsea);
		assertInDiscard(otsea);
		// The Free Peoples player is the one made to do it, and chooses which.
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("Return"));
		scn.FreepsChoose("Exert");

		// Ring-bound companions able to exert twice; not Sam, who can exert only once, nor the unbound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, smeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, aragorn, legolas, herdsman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(smeagol);

		assertEquals(2, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn, legolas);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayChooseToReturnAnUnboundCompanionToHisHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(sam, smeagol, aragorn, legolas);
		scn.MoveAlliesToTable(herdsman);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(otsea);
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertInZone(Zone.FREE_CHARACTERS, aragorn, legolas);

		scn.ShadowUseCardAction(otsea);
		assertInDiscard(otsea);
		scn.FreepsChoose("Return");

		// Unbound companions of two cultures and races; not the Ring-bound (the Ring-bearer among them), nor an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, smeagol, herdsman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertInHand(aragorn);
		assertEquals(4, scn.GetFreepsHandCount());
		assertInZone(Zone.FREE_CHARACTERS, legolas, sam, smeagol);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionTheExertionIsMadeWithNoChoiceOfWhatToDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(otsea);
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.ShadowUseCardAction(otsea);
		assertInDiscard(otsea);
		// No branch question: the return cannot be done, so the Free Peoples player goes straight to whom he exerts.
		assertFalse(scn.FreepsChoiceAvailable("Return"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(sam);

		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoRingBoundCompanionAbleToExertTwiceTheReturnIsMadeWithNoChoiceOfWhatToDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(sam, aragorn, legolas);
		// Vitality 4 with 2 wounds each: Frodo and Sam can each exert once, not twice.
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(sam, 2);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(otsea);
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		scn.ShadowUseCardAction(otsea);
		assertInDiscard(otsea);
		// No branch question: the exertion cannot be made in full, so the return is what is done.
		assertFalse(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, legolas));
		scn.FreepsChooseCard(legolas);

		assertInHand(legolas);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFourCardsInTheFreePeoplesHandRegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(otsea);
		// Four cards in the Free Peoples hand: the Free Peoples player has initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"),
				scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));
		scn.FreepsPass();

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(otsea));
		assertInZone(Zone.SHADOW_CHARACTERS, otsea);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
	}
}
