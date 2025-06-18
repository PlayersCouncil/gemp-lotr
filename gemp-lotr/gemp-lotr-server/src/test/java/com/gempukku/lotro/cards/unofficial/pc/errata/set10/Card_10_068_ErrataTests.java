package com.gempukku.lotro.cards.unofficial.pc.errata.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_068_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enquea", "60_68");
					put("darkness", "8_68");

					put("rider", "4_286");
					put("mount", "4_287");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireEnqueaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 10
		* Title: Ulaire Enquea, Thrall of The One
		* Unique: True
		* Side: SHADOW
		* Culture: Wraith
		* Twilight Cost: 6
		* Type: minion
		* Subtype: Nazgul
		* Strength: 11
		* Vitality: 4
		* Site Number: 3
		* Game Text: <b>Enduring.</b> <br>Shadow cards cannot exert Ulaire Enquea during a skirmish phase. <br><b>Skirmish:</b> If Ulaire Enquea is skirmishing, remove (1) and heal him to add a burden.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var enquea = scn.GetFreepsCard("enquea");

		assertTrue(enquea.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, enquea.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, enquea.getBlueprint().getCulture());
		assertEquals(CardType.MINION, enquea.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, enquea.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(enquea, Keyword.ENDURING));
		assertEquals(6, enquea.getBlueprint().getTwilightCost());
		assertEquals(11, enquea.getBlueprint().getStrength());
		assertEquals(4, enquea.getBlueprint().getVitality());
		assertEquals(3, enquea.getBlueprint().getSiteNumber());
	}

	@Test
	public void EnqueaCannotBeExertedByShadowCardsDuringSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, mount);

		var enquea = scn.GetShadowCard("enquea");
		var darkness = scn.GetShadowCard("darkness");
		scn.MoveMinionsToTable(enquea);
		scn.MoveCardsToHand(darkness);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.AddWoundsToChar(enquea, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, enquea);

		assertEquals(1, scn.GetWoundsOn(enquea));
		scn.FreepsResolveSkirmish(rider);
		// Freeps cards can exert Enquea fine
		assertEquals(2, scn.GetWoundsOn(enquea));
		scn.FreepsPassCurrentPhaseAction();
		// Shadow cards cannot exert Enquea
		assertFalse(scn.ShadowPlayAvailable(darkness));
	}

	@Test
	public void SkirmishActionRemoves1TwilightAndHealsEnqueaToAddBurden() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var enquea = scn.GetShadowCard("enquea");
		scn.MoveMinionsToTable(enquea);

		scn.StartGame();
		scn.AddWoundsToChar(enquea, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, enquea);
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(2, scn.GetWoundsOn(enquea));
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens()); // starts with 1 for some reason, the bid maybe?
		assertTrue(scn.ShadowActionAvailable(enquea));

		scn.ShadowUseCardAction(enquea);
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetBurdens());
	}
}
