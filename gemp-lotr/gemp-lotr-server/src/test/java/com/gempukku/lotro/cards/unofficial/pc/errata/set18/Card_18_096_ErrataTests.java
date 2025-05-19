package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_096_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "4_267");
					put("horn", "68_96");
					put("alatar", "13_28");
					put("worker", "15_135");
					put("erkenbrand", "0_59");

					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ErkenbrandsHornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 18
		* Title: *Erkenbrand's Horn
		* Side: Free Peoples
		* Culture: Rohan
		* Twilight Cost: 1
		* Type: possession
		* Subtype: 
		* Game Text: Bearer must be a [rohan] Man.<br><b>Fellowship:</b> Exert bearer to play a [rohan] follower from your draw deck. <br><b>Skirmish:</b> Discard your follower to make bearer strength +4 (or +5 if bearer is Erkenbrand).
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");

		assertTrue(horn.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, horn.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, horn.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, horn.getBlueprint().getCardType());
		//assertTrue(horn.getBlueprint().getPossessionClasses().contains(PossessionClass.HORN));
		assertEquals(1, horn.getBlueprint().getTwilightCost());
	}

	@Test
	public void HornPlaysOnRohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var horn = scn.GetFreepsCard("horn");
		scn.MoveCardsToHand(horn, eomer);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(horn));
		scn.FreepsPlayCard(eomer);
		assertTrue(scn.FreepsPlayAvailable(horn));
		assertEquals(Zone.HAND, horn.getZone());
		scn.FreepsPlayCard(horn);
		assertEquals(Zone.ATTACHED, horn.getZone());
		assertEquals(eomer, horn.getAttachedTo());
	}

	@Test
	public void HornFellowshipActionPlaysRohanFollowerFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var horn = scn.GetFreepsCard("horn");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionToTable(eomer);
		scn.AttachCardsTo(eomer, horn);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(Zone.DECK, worker.getZone());
		assertTrue(scn.FreepsActionAvailable(horn));

		scn.FreepsUseCardAction(horn);
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetWoundsOn(eomer));
        scn.FreepsChooseCardBPFromSelection(worker);

		assertEquals(Zone.SUPPORT, worker.getZone());
	}

	@Test
	public void HornSkirmishActionGivesPlus5OnErkenbrand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var horn = scn.GetFreepsCard("horn");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionToTable(erkenbrand);
		scn.AttachCardsTo(erkenbrand, horn);
		scn.MoveCardsToSupportArea(worker);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(erkenbrand, runner);
		scn.FreepsResolveSkirmish(erkenbrand);

		assertTrue(scn.FreepsActionAvailable(horn));
		assertEquals(Zone.SUPPORT, worker.getZone());
		assertEquals(7, scn.GetStrength(erkenbrand));
		scn.FreepsUseCardAction(horn);
		assertEquals(Zone.DISCARD, worker.getZone());
		assertEquals(12, scn.GetStrength(erkenbrand));
	}

	@Test
	public void HornSkirmishActionGivesPlus4OnOthers() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var horn = scn.GetFreepsCard("horn");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionToTable(eomer);
		scn.AttachCardsTo(eomer, horn);
		scn.MoveCardsToSupportArea(worker);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eomer, runner);
		scn.FreepsResolveSkirmish(eomer);

		assertTrue(scn.FreepsActionAvailable(horn));
		assertEquals(Zone.SUPPORT, worker.getZone());
		assertEquals(7, scn.GetStrength(eomer));
		scn.FreepsUseCardAction(horn);
		assertEquals(Zone.DISCARD, worker.getZone());
		assertEquals(11, scn.GetStrength(eomer));
	}
}
