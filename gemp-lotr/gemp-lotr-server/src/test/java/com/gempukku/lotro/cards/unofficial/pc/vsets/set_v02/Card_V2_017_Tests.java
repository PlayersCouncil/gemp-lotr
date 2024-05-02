package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.KillEffect;
import com.gempukku.lotro.logic.timing.Action;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class Card_V2_017_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("card", "102_17");
					put("aragorn", "101_19");
					put("veowyn", "4_270");
					put("vgamling", "5_82");
					put("twk", "1_237");
					// put other cards in here as needed for the test case
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void IWillDieAsOneofThemStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: I Will Die as One of Them
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert Aragorn three times to make each valiant companion strength +2 until the regroup phase. 
		* 	If Aragorn dies during this turn, make each valiant companion strength +1 for the rest of the turn. 
		*/

		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl card = scn.GetFreepsCard("card");

		assertEquals("I Will Die as One of Them", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IWillDieAsOneofThemBoostsValiantCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl card = scn.GetFreepsCard("card");
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl veowyn = scn.GetFreepsCard("veowyn");
		PhysicalCardImpl vgamling = scn.GetFreepsCard("vgamling");
		scn.FreepsMoveCardToHand(card, aragorn, veowyn, vgamling);

		PhysicalCardImpl twk = scn.GetShadowCard("twk");
		scn.ShadowMoveCardToHand(twk);

		scn.StartGame();

		scn.FreepsMoveCharToTable(aragorn);
		scn.FreepsPlayCard(veowyn);
		scn.FreepsPlayCard(vgamling);


		assertEquals(6, scn.GetStrength(veowyn));
		assertEquals(6, scn.GetStrength(vgamling));

		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(twk);

		scn.SkipToPhase(Phase.MANEUVER);


		var twilightInPoolBeforeCardPlay = scn.GetTwilight();

		assertTrue(scn.FreepsPlayAvailable(card));
		scn.FreepsPlayCard(card);


		assertEquals(twilightInPoolBeforeCardPlay + 1, scn.GetTwilight());

		// Valiant companion's strengths are boosted after card is played
		assertEquals(8, scn.GetStrength(veowyn));
		assertEquals(8, scn.GetStrength(vgamling));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(8, scn.GetStrength(veowyn));
		assertEquals(8, scn.GetStrength(vgamling));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(veowyn, twk);
		scn.FreepsResolveSkirmish(veowyn);


		// Valiant companions continue to have boosted strength during the skirmish phase
		scn.SkipToPhase(Phase.SKIRMISH);
		assertEquals(8, scn.GetStrength(veowyn));
		assertEquals(8, scn.GetStrength(vgamling));



		// Valiant companions have had their strength reduced to normal after the beginning of the regroup phase
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(6, scn.GetStrength(veowyn));
		assertEquals(6, scn.GetStrength(vgamling));
	}

	@Test
	public void IWillDieAsOneofThemBoostsValiantCompanionsOnDeath() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl card = scn.GetFreepsCard("card");
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl veowyn = scn.GetFreepsCard("veowyn");
		PhysicalCardImpl vgamling = scn.GetFreepsCard("vgamling");
		scn.FreepsMoveCardToHand(card, aragorn, veowyn, vgamling);

		PhysicalCardImpl twk = scn.GetShadowCard("twk");
		scn.ShadowMoveCardToHand(twk);

		scn.StartGame();

		scn.FreepsMoveCharToTable(aragorn);
		scn.FreepsPlayCard(veowyn);
		scn.FreepsPlayCard(vgamling);


		assertEquals(6, scn.GetStrength(veowyn));
		assertEquals(6, scn.GetStrength(vgamling));

		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(twk);

		scn.SkipToPhase(Phase.MANEUVER);


		var twilightInPoolBeforeCardPlay = scn.GetTwilight();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(card));
		scn.FreepsPlayCard(card);
		assertEquals(3, scn.GetWoundsOn(aragorn));


		assertEquals(twilightInPoolBeforeCardPlay + 1, scn.GetTwilight());

		// Valiant companion's strengths are boosted after card is played
		assertEquals(8, scn.GetStrength(veowyn));
		assertEquals(8, scn.GetStrength(vgamling));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(8, scn.GetStrength(veowyn));
		assertEquals(8, scn.GetStrength(vgamling));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(veowyn, twk);
		scn.FreepsResolveSkirmish(veowyn);

		scn.ApplyAdHocAction(new AbstractActionProxy() {
					@Override
					public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
						ActivateCardAction action = new ActivateCardAction(twk);
						action.appendEffect(new KillEffect(aragorn, (PhysicalCard) null, KillEffect.Cause.WOUNDS));
						return Collections.singletonList(action);
					}
				});

		assertEquals(Zone.FREE_CHARACTERS, aragorn.getZone());
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(twk);
		assertEquals(Zone.DEAD, aragorn.getZone());

		assertEquals(9, scn.GetStrength(veowyn));
		assertEquals(9, scn.GetStrength(vgamling));

		// Valiant companions continue to have boosted strength during the skirmish phase
		scn.SkipToPhase(Phase.SKIRMISH);
		assertEquals(9, scn.GetStrength(veowyn));
		assertEquals(9, scn.GetStrength(vgamling));



		// Valiant companions have had their strength reduced to normal after the beginning of the regroup phase
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(7, scn.GetStrength(veowyn));
		assertEquals(7, scn.GetStrength(vgamling));
	}
}
