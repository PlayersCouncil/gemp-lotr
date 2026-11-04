package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("voice", "13_101");
					put("axeman", "11_69");     // Axeman of Harad: [men] Man minion, no requirement; a discount beside a companion of resistance 3 or less
					put("covetous", "12_59");   // Covetous Easterling: [men] Man minion; damage +2 while skirmishing a Man
					put("trail", "12_76");      // Trail of Terror: a [men] card that is no minion (support-area condition), a start-of-maneuver trigger
					put("chief", "4_20");       // Hill Chief: a Man minion of another culture ([dunland]), vitality 2; a Shadow ability only
					put("savage", "4_17");      // Dunlending Savage: a second [dunland] Man minion, no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man to exert for Defend It and Hope
					put("defend", "4_115");     // Defend It and Hope: Maneuver, exert a [gondor] Man to wound a minion once
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VoiceoftheDesertStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Voice of the Desert, Southron Troop
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: To play, spot a [men] minion.<br>While the fellowship is in region 1, each [men] minion cannot take wounds.<br>While the fellowship is in region 2, each [men] minion is an <b>archer</b>.<br>While the fellowship is in region 3, each [men] minion is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("voice");

		assertEquals("Voice of the Desert", card.getBlueprint().getTitle());
		assertEquals("Southron Troop", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAMenMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var axeman = scn.GetShadowCard("axeman");
		var trail = scn.GetShadowCard("trail");
		var chief = scn.GetShadowCard("chief");
		scn.MoveCardsToHand(voice, axeman);

		scn.StartGame();
		scn.MoveCardsToSupportArea(trail);
		scn.MoveMinionsToTable(chief);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [men] card that is no minion and a [dunland] Man minion.
		assertInZone(Zone.SUPPORT, trail);
		assertInZone(Zone.SHADOW_CHARACTERS, chief);
		assertFalse(scn.ShadowPlayAvailable(voice));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(axeman);
		assertTrue(scn.ShadowPlayAvailable(voice));

		scn.ShadowPlayCard(voice);
		assertInZone(Zone.SHADOW_CHARACTERS, voice);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InRegionOneEachMenMinionCannotTakeWoundsAndIsNeitherArcherNorFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var voice = scn.GetShadowCard("voice");
		var covetous = scn.GetShadowCard("covetous");
		var chief = scn.GetShadowCard("chief");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(voice);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(covetous, chief, savage);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Site 3, the last site of region 1.  Covetous Easterling is the [men] minion spotted to play Voice.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowPlayAvailable(voice));
		scn.ShadowPlayCard(voice);
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.HasKeyword(voice, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(covetous, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(voice, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(covetous, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(voice));
		assertEquals(0, scn.GetWoundsOn(covetous));
		assertEquals(0, scn.GetWoundsOn(chief));

		// The two [dunland] Men can be wounded; the two [men] minions are not offered.
		scn.FreepsPlayCard(defend);
		assertTrue(scn.FreepsHasCardChoiceAvailable(chief, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(voice, covetous));
		scn.FreepsChooseCard(chief);
		assertEquals(1, scn.GetWoundsOn(chief));
		assertEquals(0, scn.GetWoundsOn(voice));
		assertEquals(0, scn.GetWoundsOn(covetous));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void InRegionTwoEachMenMinionIsAnArcherAndCanTakeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var voice = scn.GetShadowCard("voice");
		var covetous = scn.GetShadowCard("covetous");
		var chief = scn.GetShadowCard("chief");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(voice, covetous, chief);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 4, the first site of region 2.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(voice, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(covetous, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(chief, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(voice, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(covetous, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(voice));

		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(voice);
		assertEquals(1, scn.GetWoundsOn(voice));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void InRegionThreeEachMenMinionIsFierceAndNoArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var covetous = scn.GetShadowCard("covetous");
		var chief = scn.GetShadowCard("chief");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveMinionsToTable(voice, covetous, chief);
		scn.FreepsPass();

		// Site 7, the first site of region 3.
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(voice, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(covetous, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(chief, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(voice, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(covetous, Keyword.ARCHER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
