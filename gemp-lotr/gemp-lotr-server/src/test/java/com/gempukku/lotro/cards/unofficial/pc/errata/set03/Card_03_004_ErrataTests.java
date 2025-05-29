package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_03_004_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("welcome", "53_4");
					put("gimli", "1_13");
					put("thrarin", "1_27");
					put("elrond", "1_40");
					put("cantea", "1_230");
					put("lemenya", "1_232");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ARoyalWelcomeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 3
		* Title: A Royal Welcome
		* Unique: False
		* Side: FREE_PEOPLE
		* Culture: Dwarven
		* Twilight Cost: 2
		* Type: condition
		* Subtype: 
		* Game Text: Fellowship: Exert a [dwarven] companion to heal a [dwarven] ally.
		* 	Skirmish: Spot a [dwarven] companion skirmishing a minion of strength 10 or more and exert a [dwarven] ally to make that companion strength +2. 
		* 	Regroup: Exert a [dwarven] ally to heal a [dwarven] companion.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");

		assertFalse(welcome.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, welcome.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, welcome.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, welcome.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(welcome, Keyword.SUPPORT_AREA));
		assertEquals(2, welcome.getBlueprint().getTwilightCost());
	}

	@Test
	public void ARoyalWelcomeFellowshipAbilityHealsDwarvenAllyWithDwarvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var elrond = scn.GetFreepsCard("elrond");

		scn.MoveCardsToSupportArea(welcome, elrond, thrarin);
		scn.MoveCompanionToTable(gimli);


		scn.StartGame();

		scn.AddWoundsToChar(thrarin, 1);
		scn.AddWoundsToChar(gimli, 1);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(thrarin));

		assertTrue(scn.FreepsActionAvailable(welcome));
		scn.FreepsUseCardAction(welcome);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrarin));

		//Gimli's out of vitality
		assertFalse(scn.FreepsActionAvailable(welcome));
	}

	@Test
	public void ARoyalWelcomeRegroupAbilityHealsDwarvenCompWithDwarvenAlly() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var elrond = scn.GetFreepsCard("elrond");

		scn.MoveCardsToSupportArea(welcome, elrond, thrarin);
		scn.MoveCompanionToTable(gimli);

		scn.AddWoundsToChar(gimli, 2);
		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrarin));

		assertTrue(scn.FreepsActionAvailable(welcome));
		scn.FreepsUseCardAction(welcome);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(thrarin));

		//Thrarin's out of vitality
		assertFalse(scn.FreepsActionAvailable(welcome));
	}

	@Test
	public void ARoyalWelcomeSkirmishAbilityDoesNotWorkIfNoDwarfAllyVitality() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var elrond = scn.GetFreepsCard("elrond");

		scn.MoveCardsToSupportArea(welcome, elrond, thrarin);
		scn.MoveCompanionToTable(gimli);

		var cantea = scn.GetShadowCard("cantea");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.MoveMinionsToTable(cantea, lemenya);

		scn.StartGame();

		scn.AddWoundsToChar(thrarin, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, cantea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(1, scn.GetVitality(thrarin));
		assertFalse(scn.FreepsActionAvailable(welcome));
	}

	@Test
	public void ARoyalWelcomeSkirmishAbilityExertsDwarvenAllyIfDwarfSkirmishes10StrMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var elrond = scn.GetFreepsCard("elrond");

		scn.MoveCardsToSupportArea(welcome, elrond, thrarin);
		scn.MoveCompanionToTable(gimli);

		var cantea = scn.GetShadowCard("cantea");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.MoveMinionsToTable(cantea, lemenya);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(2, scn.GetVitality(thrarin));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(9, scn.GetStrength(lemenya));
		assertFalse(scn.FreepsActionAvailable(welcome));

		scn.PassCurrentPhaseActions();

		//Fierce assignment
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gimli, cantea);
		//lemenya is not fierce
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(2, scn.GetVitality(thrarin));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(10, scn.GetStrength(cantea));
		assertTrue(scn.FreepsActionAvailable(welcome));

		scn.FreepsUseCardAction(welcome);
		assertEquals(1, scn.GetVitality(thrarin));
		assertEquals(8, scn.GetStrength(gimli));
	}
}
