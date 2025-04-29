package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_048_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("stronghold", "102_48");
					put("eowyn", "5_122"); //valiant
					put("guard", "5_83"); //valiant
					put("elite", "4_265");
					put("rider", "4_286");

					put("sword", "4_272");
					put("helm", "5_89");

					put("bowman", "2_60");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void StrongholdofMyFathersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Stronghold of My Fathers
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: Fortification. To play, spot 3 [rohan] companions (or 2 valiant companions).
		* 	Response: If a [rohan] companion is about to take a wound, discard this or 2 other [rohan] possessions to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stronghold");

		assertEquals("Stronghold of My Fathers", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void StrongholdofMyFathersCanSpot3RohanCompanionsToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var stronghold = scn.GetFreepsCard("stronghold");
		var eowyn = scn.GetFreepsCard("eowyn");
		var elite = scn.GetFreepsCard("elite");
		var rider = scn.GetFreepsCard("rider");
		scn.FreepsMoveCardToHand(stronghold, eowyn, elite, rider);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(stronghold));
		scn.FreepsPlayCard(eowyn);
		assertFalse(scn.FreepsPlayAvailable(stronghold));
		scn.FreepsPlayCard(elite);
		assertFalse(scn.FreepsPlayAvailable(stronghold));
		scn.FreepsPlayCard(rider);
		assertTrue(scn.FreepsPlayAvailable(stronghold));
	}

	@Test
	public void StrongholdofMyFathersCanSpot2ValiantCompanionsToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var stronghold = scn.GetFreepsCard("stronghold");
		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		scn.FreepsMoveCardToHand(stronghold, eowyn, guard);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(stronghold));
		scn.FreepsPlayCard(eowyn);
		assertFalse(scn.FreepsPlayAvailable(stronghold));
		scn.FreepsPlayCard(guard);
		assertTrue(scn.FreepsPlayAvailable(stronghold));
	}

	@Test
	public void StrongholdofMyFathersCanDiscardSelfToPreventWoundToRohanCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var stronghold = scn.GetFreepsCard("stronghold");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.FreepsMoveCharToTable(eowyn);
		scn.FreepsMoveCardToSupportArea(stronghold);

		var bowman = scn.GetShadowCard("bowman");
		scn.ShadowMoveCharToTable(bowman);

		scn.StartGame();

		scn.SkipToArcheryWounds();

		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(Zone.SUPPORT, stronghold.getZone());

		scn.FreepsChooseCard(eowyn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(Zone.DISCARD, stronghold.getZone());
	}

	@Test
	public void StrongholdofMyFathersCanDiscard2OtherRohanPossessionsToPreventWoundToRohanCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var stronghold = scn.GetFreepsCard("stronghold");
		var eowyn = scn.GetFreepsCard("eowyn");
		var sword = scn.GetFreepsCard("sword");
		var helm = scn.GetFreepsCard("helm");
		scn.FreepsMoveCharToTable(eowyn);
		scn.AttachCardsTo(eowyn, sword);
		scn.AttachCardsTo(eowyn, helm);
		scn.FreepsMoveCardToSupportArea(stronghold);

		var bowman = scn.GetShadowCard("bowman");
		scn.ShadowMoveCharToTable(bowman);

		scn.StartGame();

		scn.SkipToArcheryWounds();

		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(Zone.SUPPORT, stronghold.getZone());
		assertEquals(Zone.ATTACHED, sword.getZone());
		assertEquals(Zone.ATTACHED, helm.getZone());

		scn.FreepsChooseCard(eowyn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsChoiceAvailable("Discard Stronghold"));
		assertTrue(scn.FreepsChoiceAvailable("Discard 2 other"));

		scn.FreepsChoose("Discard 2 other");

		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(Zone.SUPPORT, stronghold.getZone());
		assertEquals(Zone.DISCARD, sword.getZone());
		assertEquals(Zone.DISCARD, helm.getZone());
	}
}
