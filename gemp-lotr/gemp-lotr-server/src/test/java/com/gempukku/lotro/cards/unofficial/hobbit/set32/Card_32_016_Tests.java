package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_016_Tests
{

	// The Hobbit table: the Hobbit path (site 7 is Erebor 30_55, a mountain, where Smaug may stand) and Bilbo,
	// Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arrow", "32_16");
					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally, archer, vitality 3; a Regroup ability, not used
					put("percy", "32_19");      // Percy, Bard's Lieutenant: [esgaroth] Man ally; lets [esgaroth] Men bearing a weapon take part; a Response, not used
					// Minions of two cultures and races with vitality 3, so that two wounds leave them standing.
					put("watchful", "30_40");   // Watchful Orc: [gundabad] Orc, vitality 3; a play trigger and a Shadow ability, not used
					put("troll", "32_31");      // Demolition Troll: [moria] Troll, vitality 3; a Skirmish ability, not used
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, vitality 2; an Assignment ability, not used
					// Smaug, The Golden: [smaug] Dragon minion, vitality 5; discarded if not at a mountain, a Regroup ability
					put("smaug", "30_66");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BlackArrowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Black Arrow
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Esgaroth
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged Weapon
		 * Strength: 1
		 * Game Text: Bearer must be Bard.<br><b>Archery:</b> Exert Bard and discard this possession to wound a minion twice. If that minion is Smaug, wound it again.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arrow");

		assertEquals("Black Arrow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ESGAROTH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void CanBePlayedOnlyOnBard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var bard = scn.GetFreepsCard("bard");
		var percy = scn.GetFreepsCard("percy");
		// Percy, an [esgaroth] Man ally who is not Bard, and Bilbo, the Ring-bearer.
		scn.MoveAlliesToTable(percy);
		scn.MoveCardsToHand(arrow);

		scn.StartGame();

		assertInHand(arrow);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(arrow));

		scn.MoveAlliesToTable(bard);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(arrow));
		// Bard is the one bearer it may go on, so it goes on him unasked.
		scn.FreepsPlayCard(arrow);
		assertAttachedTo(arrow, bard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryAbilityExertsBardAndDiscardsTheArrowToWoundAMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var bard = scn.GetFreepsCard("bard");
		var bilbo = scn.GetRingBearer();
		var watchful = scn.GetShadowCard("watchful");
		var troll = scn.GetShadowCard("troll");
		var aggressor = scn.GetShadowCard("aggressor");
		var smaug = scn.GetShadowCard("smaug");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveAlliesToTable(bard);
		scn.AttachCardsTo(bard, arrow);
		assertAttachedTo(arrow, bard);
		// On to Erebor, a mountain: Smaug stands.
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(watchful, troll, aggressor, smaug);
		scn.AddWoundsToChar(aggressor, 1);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		scn.SkipToPhase(Phase.MANEUVER);

		// The maneuver phase, the one before: not offered.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(arrow));

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(arrow));
		scn.FreepsUseCardAction(arrow);

		assertEquals(1, scn.GetWoundsOn(bard));
		assertInZone(Zone.DISCARD, arrow);

		// Minions of three cultures and races, an exhausted one and Smaug among them; not Bilbo, nor Bard.
		assertTrue(scn.FreepsHasCardChoiceAvailable(watchful, troll, aggressor, smaug));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, bard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troll);

		// Two wounds on the Troll; Smaug is on the table and is not wounded, nor is anyone else.
		assertEquals(2, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(smaug));
		assertEquals(0, scn.GetWoundsOn(watchful));
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void SmaugChosenIsWoundedThreeTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var bard = scn.GetFreepsCard("bard");
		var watchful = scn.GetShadowCard("watchful");
		var smaug = scn.GetShadowCard("smaug");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveAlliesToTable(bard);
		scn.AttachCardsTo(bard, arrow);
		assertAttachedTo(arrow, bard);
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(watchful, smaug);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(smaug));
		scn.FreepsUseCardAction(arrow);
		assertTrue(scn.FreepsHasCardChoiceAvailable(watchful, smaug));
		scn.FreepsChooseCard(smaug);

		// Twice, and again: three wounds of Smaug's five.
		assertEquals(3, scn.GetWoundsOn(smaug));
		assertEquals(0, scn.GetWoundsOn(watchful));
		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AnExhaustedMinionChosenIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var bard = scn.GetFreepsCard("bard");
		var watchful = scn.GetShadowCard("watchful");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.MoveAlliesToTable(bard);
		scn.AttachCardsTo(bard, arrow);
		assertAttachedTo(arrow, bard);
		scn.MoveMinionsToTable(watchful, aggressor);
		// Vitality 2 with 1 wound: exhausted, so the first wound kills it.
		scn.AddWoundsToChar(aggressor, 1);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		scn.SkipToPhase(Phase.ARCHERY);

		scn.FreepsUseCardAction(arrow);
		assertTrue(scn.FreepsHasCardChoiceAvailable(watchful, aggressor));
		scn.FreepsChooseCard(aggressor);

		assertInZone(Zone.DISCARD, aggressor);
		assertEquals(0, scn.GetWoundsOn(watchful));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenBardIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var bard = scn.GetFreepsCard("bard");
		var percy = scn.GetFreepsCard("percy");
		var watchful = scn.GetShadowCard("watchful");

		scn.StartGame();
		// Percy, another [esgaroth] ally, could exert; only Bard pays.
		scn.MoveAlliesToTable(bard, percy);
		scn.AttachCardsTo(bard, arrow);
		assertAttachedTo(arrow, bard);
		// Vitality 3 with 2 wounds: he cannot exert.
		scn.AddWoundsToChar(bard, 2);
		assertEquals(2, scn.GetWoundsOn(bard));
		scn.MoveMinionsToTable(watchful);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(1, scn.GetVitality(bard));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(arrow));
		assertAttachedTo(arrow, bard);
		assertEquals(0, scn.GetWoundsOn(percy));
		assertEquals(0, scn.GetWoundsOn(watchful));
	}
}
