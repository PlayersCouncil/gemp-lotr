package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_023_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the Shadow phase of
	// turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer, and which is no battleground.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("azog", "31_23");

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion; a fellowship ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a fellowship ability not used
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] companion, vitality 3; a maneuver ability
					put("dain", "32_2");        // Dain Ironfoot, Lord of the Iron Hills: [dwarven] ally; Thorin +1, a regroup offer

					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc; an assignment ability not used
					// Mounts of two cultures, and a possession that is no mount.  The Warg's and the Mace's abilities are not
					// used; the Vicious Warg's play trigger wants a battleground.
					put("warg", "31_27");       // Warg: [gundabad] mount, bearer a [gundabad] Orc
					put("vicious", "32_32");    // Vicious Warg: [moria] mount, bearer an Orc
					put("mace", "32_29");       // Azog's Mace: [moria] hand weapon, bearer an Orc
					put("pony", "33_52");       // Bilbo's Pony: the Free Peoples player's [shire] mount, bearer Bilbo
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void AzogStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Azog, The Defiler
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Damage +1</b>.
		 * Each time you play a mount, you may make the Free Peoples player exert a [dwarven] companion.
		 */

		var scn = GetScenario();

		var card = scn.GetShadowCard("azog");

		assertEquals("Azog", card.getBlueprint().getTitle());
		assertEquals("The Defiler", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachMountPlayedMayMakeTheFreePeoplesPlayerExertADwarvenCompanionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var aggressor = scn.GetShadowCard("aggressor");
		var warg = scn.GetShadowCard("warg");
		var vicious = scn.GetShadowCard("vicious");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var dain = scn.GetFreepsCard("dain");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin, dwalin);
		scn.MoveAlliesToTable(dain);
		// Vitality 3 with 2 wounds: Dwalin is exhausted.
		scn.AddWoundsToChar(dwalin, 2);
		scn.MoveCardsToHand(warg, vicious);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(dwalin));
		assertInZone(Zone.HAND, warg, vicious);
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(azog, aggressor);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		scn.ShadowPlayCard(warg);
		scn.ShadowChooseCard(azog);
		assertAttachedTo(warg, azog);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(azog));
		scn.ShadowAcceptOptionalTrigger();
		// The Free Peoples player chooses: a [dwarven] companion who can exert; not the exhausted Dwalin, not the
		// [dwarven] ally, not the [shire] Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dwalin, dain, bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(balin);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(2, scn.GetWoundsOn(dwalin));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		// A mount of another culture is offered again; declined, nobody exerts.  Azog bears a mount already, so the
		// Aggressor is its one bearer and takes it unasked.
		scn.ShadowPlayCard(vicious);
		assertAttachedTo(vicious, aggressor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(azog));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AzogsOwnPlayAndAPossessionThatIsNoMountAreAnsweredByNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var mace = scn.GetShadowCard("mace");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveCardsToHand(azog, mace);

		scn.StartGame();
		assertInZone(Zone.HAND, azog, mace);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Azog is no mount: his own play sets nothing off.
		scn.ShadowPlayCard(azog);
		assertInZone(Zone.SHADOW_CHARACTERS, azog);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));

		scn.ShadowPlayCard(mace);
		assertAttachedTo(mace, azog);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMountPlayedIsNotOfferedWithNoDwarvenCompanionAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var warg = scn.GetShadowCard("warg");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain);
		// Vitality 4 with 3 wounds: Thorin, the only [dwarven] companion, is exhausted; the [dwarven] ally is fresh.
		scn.AddWoundsToChar(thorin, 3);
		scn.MoveCardsToHand(warg);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertInZone(Zone.HAND, warg);
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(azog);
		scn.FreepsPass();

		scn.ShadowPlayCard(warg);
		assertAttachedTo(warg, azog);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMountTheFreePeoplesPlayerPlaysIsAnsweredByNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var pony = scn.GetFreepsCard("pony");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveCardsToHand(pony);

		scn.StartGame();
		assertInZone(Zone.HAND, pony);
		// Azog laid in the fellowship phase, where the minions of this turn stand later (Q-0228).
		scn.MoveMinionsToTable(azog);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(pony);
		assertAttachedTo(pony, bilbo);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
