package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_005_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The maneuver phase
	// of turn 1 is at The Troll Hoard (site 2); the minions are laid in the Shadow phase.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("army", "32_5");

					put("dain", "32_2");        // Dain Ironfoot, Lord of the Iron Hills: a second [dwarven] ally, no companion; his regroup trigger not reached
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] companion; a maneuver ability not used
					put("dori", "30_8");        // Dori, Really a decent fellow: [dwarven] follower; his Aid declined
					put("ori", "30_18");        // Ori, Dwarven Chronicler: [dwarven] follower; his transfer trigger names Balin, not in play
					put("thrush", "31_42");     // Old Thrush, Messenger Bird: a [shire] follower; his Aid declined
					put("axe", "31_1");         // Dwarven Axe: a [dwarven] possession that is no follower, on Dain

					put("azog", "32_28");       // Azog, Commander of the Wolves Army: [moria] Orc; a maneuver ability at a battleground
					put("alfrid", "32_25");     // Alfrid, Lickspittle: [men] Man; his texts answer [esgaroth] allies, none in play
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void IronHillsArmyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Iron Hills Army
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Ally
		 * Subtype: Dwarf
		 * Home: 9H
		 * Strength: 9
		 * Vitality: 3
		 * Game Text: <b>Damage +1</b>.
		 * 	<b>Maneuver:</b> If you can spot more minions than companions, exert Iron Hills Army and discard a [dwarven] follower to make an opponent discard a minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("army");

		assertEquals("Iron Hills Army", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 9)));
	}

	@Test
	public void ManeuverActionExertsAndDiscardsADwarvenFollowerToMakeTheShadowPlayerDiscardAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var dain = scn.GetFreepsCard("dain");
		var dori = scn.GetFreepsCard("dori");
		var ori = scn.GetFreepsCard("ori");
		var thrush = scn.GetFreepsCard("thrush");
		var axe = scn.GetFreepsCard("axe");
		var bilbo = scn.GetRingBearer();
		var azog = scn.GetShadowCard("azog");
		var alfrid = scn.GetShadowCard("alfrid");
		// Bilbo is the one companion; the two allies beside him are no companions, so 2 minions are more.
		scn.MoveAlliesToTable(army, dain);
		scn.MoveCardsToSupportArea(dori, thrush);

		scn.StartGame();
		scn.AttachCardsTo(bilbo, ori);
		scn.AttachCardsTo(dain, axe);
		assertInZone(Zone.SUPPORT, army, dain, dori, thrush);
		assertAttachedTo(ori, bilbo);
		assertAttachedTo(axe, dain);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog, alfrid);
		assertInZone(Zone.SHADOW_CHARACTERS, azog, alfrid);
		assertEquals(0, scn.GetWoundsOn(army));

		scn.SkipToPhase(Phase.MANEUVER);
		// The followers' Aids, offered as the phase starts, are declined.
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(army));
		scn.FreepsUseCardAction(army);

		// A [dwarven] follower in the support area and one that Bilbo bears; not a follower of another culture, nor
		// a [dwarven] card that is no follower.
		assertTrue(scn.FreepsHasCardChoiceAvailable(dori, ori));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thrush, axe));
		scn.FreepsChooseCard(ori);
		assertEquals(1, scn.GetWoundsOn(army));
		assertInZone(Zone.DISCARD, ori);
		assertInZone(Zone.SUPPORT, dori, thrush);
		assertAttachedTo(axe, dain);

		// The Shadow player chooses which of his minions goes.
		assertTrue(scn.ShadowHasCardChoiceAvailable(azog, alfrid));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(alfrid);
		assertInZone(Zone.DISCARD, alfrid);
		assertInZone(Zone.SHADOW_CHARACTERS, azog);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithAsManyCompanionsAsMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var dain = scn.GetFreepsCard("dain");
		var nori = scn.GetFreepsCard("nori");
		var dori = scn.GetFreepsCard("dori");
		var azog = scn.GetShadowCard("azog");
		var alfrid = scn.GetShadowCard("alfrid");
		// Bilbo and Nori against 2 minions; the allies are not counted.
		scn.MoveCompanionsToTable(nori);
		scn.MoveAlliesToTable(army, dain);
		scn.MoveCardsToSupportArea(dori);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, army, dain, dori);
		assertInZone(Zone.FREE_CHARACTERS, nori);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog, alfrid);
		assertInZone(Zone.SHADOW_CHARACTERS, azog, alfrid);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(army));
		assertEquals(0, scn.GetWoundsOn(army));
		assertInZone(Zone.SUPPORT, dori);
	}

	@Test
	public void NotOfferedWhileIronHillsArmyIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var dori = scn.GetFreepsCard("dori");
		var azog = scn.GetShadowCard("azog");
		var alfrid = scn.GetShadowCard("alfrid");
		scn.MoveAlliesToTable(army);
		scn.MoveCardsToSupportArea(dori);

		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(army, 2);
		assertEquals(2, scn.GetWoundsOn(army));
		assertInZone(Zone.SUPPORT, dori);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog, alfrid);
		assertInZone(Zone.SHADOW_CHARACTERS, azog, alfrid);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(army));
		assertEquals(2, scn.GetWoundsOn(army));
		assertInZone(Zone.SUPPORT, dori);
	}

	@Test
	public void NotOfferedWithNoDwarvenFollowerInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var dain = scn.GetFreepsCard("dain");
		var thrush = scn.GetFreepsCard("thrush");
		var axe = scn.GetFreepsCard("axe");
		var azog = scn.GetShadowCard("azog");
		var alfrid = scn.GetShadowCard("alfrid");
		scn.MoveAlliesToTable(army, dain);
		scn.MoveCardsToSupportArea(thrush);

		scn.StartGame();
		scn.AttachCardsTo(dain, axe);
		assertInZone(Zone.SUPPORT, army, dain, thrush);
		assertAttachedTo(axe, dain);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog, alfrid);
		assertInZone(Zone.SHADOW_CHARACTERS, azog, alfrid);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(army));
		assertEquals(0, scn.GetWoundsOn(army));
		assertInZone(Zone.SUPPORT, thrush);
	}
}
