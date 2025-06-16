package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_04_073_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dh", "54_73");
					put("merry", "1_302");
					put("pippin", "1_306");
					put("fatty", "18_107");

					put("condition0", "1_133");
					put("condition1", "1_183");
					put("event0", "1_170");
					put("event1", "1_215");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 4
		* Title: Legolas, Dauntless Hunter
		* Unique: True
		* Side: FREE_PEOPLE
		* Culture: Elven
		* Twilight Cost: 2
		* Type: companion
		* Subtype: Elf
		* Strength: 6
		* Vitality: 3
		* Signet: Aragorn
		* Game Text: <b>Archer.</b>  <br>Each time a Shadow event or Shadow condition is played, remove (1) for each unbound Hobbit you can spot (limit (2)).
		*/

		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("dh");

		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void LegolasRemoves1ExtraWhenAnEventOrConditionIsPlayedWith1UnboundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.MoveCompanionToTable("dh", "merry");

		var event0 = scn.GetShadowCard("event0");
		var event1 = scn.GetShadowCard("event1");
		var condition0 = scn.GetShadowCard("condition0");
		var condition1 = scn.GetShadowCard("condition1");

		scn.MoveCardsToHand(event0, event1, condition0, condition1);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(5, scn.GetTwilight());

		scn.ShadowPlayCard(event0);
		scn.DismissRevealedCards();
		assertEquals(4, scn.GetTwilight()); // 0-cost event, but extra 1 from DH

		scn.ShadowPlayCard(event1);
		scn.ShadowChoose("0");
		assertEquals(2, scn.GetTwilight()); // 1-cost event, but extra 1 from DH

		scn.ShadowPlayCard(condition1);
		assertEquals(0, scn.GetTwilight()); // 1-cost condition, but extra 1 from DH

	}

	@Test
	public void LegolasRemoves2ExtraWhenAnEventOrConditionIsPlayedWith2UnboundHobbits() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.MoveCompanionToTable("dh", "merry", "pippin");

		var event0 = scn.GetShadowCard("event0");
		var event1 = scn.GetShadowCard("event1");
		var condition0 = scn.GetShadowCard("condition0");
		var condition1 = scn.GetShadowCard("condition1");

		scn.MoveCardsToHand(event0, event1, condition0, condition1);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(6, scn.GetTwilight());

		scn.ShadowPlayCard(event0);
		assertEquals(4, scn.GetTwilight()); // 0-cost event, but extra 2 from DH
		scn.DismissRevealedCards();

		scn.ShadowPlayCard(event1);
		scn.ShadowChoose("0");
		assertEquals(1, scn.GetTwilight()); // 1-cost event, but extra 2 from DH

		scn.ShadowPlayCard(condition1);
		assertEquals(0, scn.GetTwilight()); // 1-cost condition, but extra 2 from DH that don't affect the 0 pool
	}

	@Test
	public void LegolasRemoves2ExtraWhenAnEventOrConditionIsPlayedWith3UnboundHobbits() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.MoveCompanionToTable("dh", "merry", "pippin", "fatty");

		var event0 = scn.GetShadowCard("event0");
		var event1 = scn.GetShadowCard("event1");
		var condition0 = scn.GetShadowCard("condition0");
		var condition1 = scn.GetShadowCard("condition1");

		scn.MoveCardsToHand(event0, event1, condition0, condition1);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetTwilight());

		scn.ShadowPlayCard(event0);
		assertEquals(5, scn.GetTwilight()); // 0-cost event, but extra 2 from DH
		scn.DismissRevealedCards();

		scn.ShadowPlayCard(event1);
		scn.ShadowChoose("0");
		assertEquals(2, scn.GetTwilight()); // 1-cost event, but extra 2 from DH

		scn.ShadowPlayCard(condition1);
		assertEquals(0, scn.GetTwilight()); // 1-cost condition, but extra 2 from DH that is only partialy taken out
	}

	@Test
	public void LegolasPermitsA0CostEventOrConditionToBePlayedWith0Twilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.MoveCompanionToTable("dh", "merry");

		var event0 = scn.GetShadowCard("event0");
		var event1 = scn.GetShadowCard("event1");
		var condition0 = scn.GetShadowCard("condition0");
		var condition1 = scn.GetShadowCard("condition1");

		scn.MoveCardsToHand(event0, event1, condition0, condition1);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.SetTwilight(2);

		scn.ShadowPlayCard(event1);
		scn.ShadowChoose("0");
		assertEquals(0, scn.GetTwilight()); // 1-cost event, but extra 1 from DH

		assertTrue(scn.ShadowPlayAvailable(event0));
		assertTrue(scn.ShadowPlayAvailable(condition0));

		scn.ShadowPlayCard(event0);
		scn.DismissRevealedCards();
		assertEquals(0, scn.GetTwilight()); // 0-cost event, but extra 1 from DH that can't be taken out

		scn.ShadowPlayCard(condition0);
		assertEquals(0, scn.GetTwilight()); // 0-cost condition, but extra 1 from DH that can't be taken out
	}
}
