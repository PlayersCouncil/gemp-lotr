
package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
                new HashMap<>() {{
                    put("asfaloth", "1_31");
                    put("arwen", "3_7");
                    put("legolas", "1_50");
                    put("erestor", "3_14");
                    put("orophin", "1_56");
                }},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AsfalothStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Asfaloth
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 2
		 * Game Text: Bearer must be an Elf. When played on Arwen, Asfaloth's twilight cost is -2.<br>While at a plains site, bearer is strength +2. Discard Asfaloth when at an underground site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("asfaloth");

		assertEquals("Asfaloth", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeAnElf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCardsToHand(asfaloth, arwen, erestor, orophin);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(asfaloth));
		scn.FreepsPlayCard(orophin);
		assertTrue(scn.FreepsPlayAvailable(asfaloth));
		scn.FreepsPlayCard(arwen);
		scn.FreepsPlayCard(asfaloth);
		assertEquals(2, scn.FreepsGetCardChoiceCount()); // can go on either arwen or orophin
	}

	@Test
	public void TwilightCost2OnNonArwen() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(erestor, orophin);
		scn.MoveCardsToHand(asfaloth);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(asfaloth);
		scn.FreepsChooseCard(erestor);
		assertEquals(2, scn.GetTwilight());
		assertAttachedTo(asfaloth, erestor);
	}

	@Test
	public void TwilightCost0OnArwen() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(erestor, orophin);
		scn.MoveCardsToHand(asfaloth);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(asfaloth);
		scn.FreepsChooseCard(arwen);
		assertEquals(0, scn.GetTwilight());
		assertAttachedTo(asfaloth, arwen);
	}

	@Test
	public void BearerIsStrengthPlus2AtPlains() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(erestor, orophin);
		scn.MoveCardsToHand(asfaloth);

		//cheating to ensure site 2 qualifies
        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), null, Keyword.PLAINS));

		scn.StartGame();

		assertEquals(6, scn.GetStrength(arwen));
		scn.FreepsPlayCard(asfaloth);
		scn.FreepsChooseCard(arwen);
		assertEquals(8, scn.GetStrength(arwen));
		scn.FreepsPass();

		assertEquals(10, scn.GetStrength(arwen));
	}

	@Test
	public void SelfDiscardsAtUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(erestor, orophin);
		scn.MoveCardsToHand(asfaloth);

		//cheating to ensure site 2 qualifies
        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), null, Keyword.UNDERGROUND));

		scn.StartGame();

		scn.FreepsPlayCard(asfaloth);
		scn.FreepsChooseCard(arwen);
		assertEquals(Zone.ATTACHED, asfaloth.getZone());
		scn.FreepsPass();

		assertEquals(Zone.DISCARD, asfaloth.getZone());
	}

	@Test
	public void TransferBetweenAllyAndCompanionPaysTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		// Bug reported: https://play.lotrtcgpc.net/gemp-lotr/game.html?replayId=FallsLover$ilpahhdbsdmp8i2x
		// Site 6 permitted multiple transfers between Rumil and Legolas on site 6 with 0 twilight added to the pool.

		//Pre-game setup
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(arwen, legolas);
		scn.MoveAlliesToTable(erestor, orophin);
		scn.MoveCardsToHand(asfaloth);

		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 10));

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(asfaloth);
		scn.FreepsChooseCard(erestor);
		assertEquals(2, scn.GetTwilight());

		scn.SkipToSite(3);

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsTransferAvailable(asfaloth));
		scn.FreepsTransferCard(asfaloth);
		scn.FreepsChooseCard(legolas);
		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void TransferredToArwenItCostsItsPrintedTwoAndNotTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(arwen, legolas);
		scn.AttachCardsTo(legolas, asfaloth);

		scn.StartGame();

		// "When played on Arwen, Asfaloth's twilight cost is -2": a transfer pays the twilight cost again and is
		// not a play (CR 4.2 "transfer"), so moving it to Arwen costs the printed 2.  She is the only other Elf.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsTransferAvailable(asfaloth));
		scn.FreepsTransferCard(asfaloth);
		assertAttachedTo(asfaloth, arwen);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
