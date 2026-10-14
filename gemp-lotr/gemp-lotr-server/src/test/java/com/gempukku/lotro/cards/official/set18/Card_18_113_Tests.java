package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "18_113");
					// Bilbo, Well-spoken Gentlehobbit: [shire] Hobbit ally, home 1F; a fellowship ability that needs a
					// tale, and none is here
					put("bilbo", "2_96");
					// Sam, Son of Hamfast: a Ring-bound Hobbit companion who is neither Bilbo nor Frodo; his abilities
					// are used only on purpose
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli is the Ring-bearer, so that the Frodo here is not.
	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "18_113");
					// Bilbo, Melancholy Hobbit: the Bilbo who is a companion; his start-of-skirmish trigger is not
					// reached here
					put("bilbo", "12_119");
					// Frodo, Frenzied Fighter: the one Frodo who can stand beside another Ring-bearer; his skirmish
					// ability is not reached here
					put("frodo", "13_149");
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Sting, Elven Long Knife
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +1
		 * Vitality: +1
		 * Resistance: +1
		 * Game Text: Bearer must be Bilbo or Frodo.
		 * 	<b>Regroup:</b> Exert bearer to remove a threat.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sting");

		assertEquals("Sting", card.getBlueprint().getTitle());
		assertEquals("Elven Long Knife", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void CanBePlayedOnFrodoOrOnTheAllyBilboButNotOnAnotherHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var frodo = scn.GetRingBearer();
		var bilbo = scn.GetFreepsCard("bilbo");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToHand(sting);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(sting));
		scn.FreepsPlayCard(sting);

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, bilbo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam));
		scn.FreepsChooseCard(bilbo);

		assertAttachedTo(sting, bilbo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedOnTheCompanionBilboOrOnAFrodoWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var sting = scn.GetFreepsCard("sting");
		var gimli = scn.GetRingBearer();
		var bilbo = scn.GetFreepsCard("bilbo");
		var frodo = scn.GetFreepsCard("frodo");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(bilbo, frodo, sam);
		scn.MoveCardsToHand(sting);

		scn.StartGame();

		scn.FreepsPlayCard(sting);

		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, frodo));
		// Neither the Ring-bearer nor a Ring-bound Hobbit of another title.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, sam));
		scn.FreepsChooseCard(frodo);

		assertAttachedTo(sting, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionExertsBearerToRemoveAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, sting);

		scn.StartGame();
		// Two threats want two companions.
		scn.AddThreats(2);

		// No minion is played, so the game goes from the Shadow phase straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sting));

		scn.FreepsUseCardAction(sting);

		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionExertsAnAllyBearerAndNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var frodo = scn.GetRingBearer();
		var bilbo = scn.GetFreepsCard("bilbo");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(bilbo, sting);

		scn.StartGame();
		scn.AddThreats(2);

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sting));

		scn.FreepsUseCardAction(sting);

		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithBearerExhaustedTheRegroupActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, sting);

		scn.StartGame();
		scn.AddThreats(1);
		// Vitality 4, +1 from Sting: 4 wounds leave him exhausted.
		scn.AddWoundsToChar(frodo, 4);

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(1, scn.GetThreats());
		assertEquals(4, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sting));
	}
}
