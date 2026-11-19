package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_019_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The [esgaroth] allies'
	// home is site 6 (Esgaroth): the boards stand at sites 2 and 3, where no ally here is at home.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("percy", "32_19");

					put("bard", "30_22");         // Bard, The Bowman: [esgaroth] Man ally, archer; a regroup ability not used
					put("arrow", "32_16");        // Black Arrow: [esgaroth] ranged weapon on Bard; an archery ability not used
					// Esgaroth Volunteers: [esgaroth] Man ally, archer, vitality 4; a maneuver ability not used
					put("volunteers", "32_17");
					put("weapon", "32_18");       // Iron-Forged Weapon: [esgaroth] hand weapon (bearer a Man); its skirmish ability not used
					put("weapon2", "32_18");
					put("bain", "32_15");         // Baïn, Son of Bard: [esgaroth] Man ally, no archer; a start-of-turn trigger, laid after it
					put("coat", "1_101");         // Coat of Mail: [gondor] armor for a Man, no weapon (no Hobbit-set item an [esgaroth] Man can bear is one)
					put("betsy", "33_19");        // Betsy Butterbur: [gandalf] Man ally, home 1H; cannot take wounds
					put("dain", "32_2");          // Dain Ironfoot, Lord of the Iron Hills: [dwarven] ally; his regroup trigger has nothing to pay with

					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					// Orkish Aggressor: [gundabad] Orc, vitality 2; an assignment ability not used. Each bears a Warg
					// 31_27 ([gundabad] mount, vitality +1, fierce): "Regroup: If you cannot spot Thorin, exert bearer
					// to wound an ally."
					put("aggressor", "30_39");
					put("aggressor2", "30_39");
					put("warg", "31_27");
					put("warg2", "31_27");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void PercyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Percy, Bard's Lieutenant
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Esgaroth
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Man
		 * Home: 6H
		 * Strength: 6
		 * Vitality: 3
		 * Game Text: <b>Archer</b>.<br>Each [esgaroth] Man bearing a weapon may participate in archery fire and skirmishes.
		 * 	<br><b>Response:</b> If another [esgaroth] ally is about to take a wound, exert Percy to prevent that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("percy");

		assertEquals("Percy", card.getBlueprint().getTitle());
		assertEquals("Bard's Lieutenant", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ESGAROTH, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 6)));
	}

	@Test
	public void OncePercyIsPlayedEachEsgarothManBearingAWeaponTakesPartInArcheryFireAndSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var percy = scn.GetFreepsCard("percy");
		var bard = scn.GetFreepsCard("bard");
		var arrow = scn.GetFreepsCard("arrow");
		var volunteers = scn.GetFreepsCard("volunteers");
		var weapon = scn.GetFreepsCard("weapon");
		var weapon2 = scn.GetFreepsCard("weapon2");
		var bain = scn.GetFreepsCard("bain");
		var coat = scn.GetFreepsCard("coat");
		var betsy = scn.GetFreepsCard("betsy");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Turn 3, at The Troll Hoard (site 2), which makes no offer of its own: no Man or [dwarven] companion is in
		// play yet to bear the Iron-Forged Weapons (F-0800).
		scn.SkipToSite(2);
		scn.MoveAlliesToTable(bard, volunteers, bain, betsy);
		scn.AttachCardsTo(bard, arrow);
		scn.AttachCardsTo(volunteers, weapon);
		scn.AttachCardsTo(betsy, weapon2);
		scn.AttachCardsTo(bain, coat);
		scn.MoveCardsToHand(percy);
		scn.FreepsRefreshDecision();
		assertAttachedTo(arrow, bard);
		assertAttachedTo(weapon, volunteers);
		assertAttachedTo(weapon2, betsy);
		assertAttachedTo(coat, bain);
		assertInHand(percy);
		assertEquals(2, scn.GetCurrentSiteNumber());

		// Away from home, no ally takes part, and the archers add nothing.
		assertFalse(scn.FreepsCanAssign(bard));
		assertFalse(scn.FreepsCanAssign(volunteers));
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(percy);
		// Bard with a ranged weapon and the Volunteers with a hand weapon, both [esgaroth] Men, may be assigned;
		// Baïn bears an item that is no weapon, Betsy is a Man of another culture, and Percy himself bears nothing.
		assertTrue(scn.FreepsCanAssign(bard));
		assertTrue(scn.FreepsCanAssign(volunteers));
		assertFalse(scn.FreepsCanAssign(bain));
		assertFalse(scn.FreepsCanAssign(betsy));
		assertFalse(scn.FreepsCanAssign(percy));
		// The two armed archers count toward the fellowship archery total, and Percy, an archer, does not.
		assertEquals(2, scn.GetFreepsArcheryTotal());

		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		// On to Rivendell (site 3): the archery wounds both go on the one minion, unasked.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(volunteers));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(betsy));
		scn.FreepsAssignToMinions(volunteers, troop);
		assertTrue(scn.IsCharAssignedAgainst(volunteers, troop));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ResponseExertsPercyToPreventAWoundToAnotherEsgarothAllyWhileHeCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var percy = scn.GetFreepsCard("percy");
		var bard = scn.GetFreepsCard("bard");
		var volunteers = scn.GetFreepsCard("volunteers");
		var aggressor = scn.GetShadowCard("aggressor");
		var aggressor2 = scn.GetShadowCard("aggressor2");
		var warg = scn.GetShadowCard("warg");
		var warg2 = scn.GetShadowCard("warg2");
		scn.MoveAlliesToTable(percy, bard, volunteers);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor, aggressor2);
		scn.AttachCardsTo(aggressor, warg);
		scn.AttachCardsTo(aggressor2, warg2);
		assertAttachedTo(warg, aggressor);
		assertAttachedTo(warg2, aggressor2);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetWoundsOn(percy));
		assertEquals(0, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetWoundsOn(volunteers));
		scn.FreepsPass();
		scn.ShadowUseCardAction(warg);
		scn.ShadowChooseCard(bard);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(percy));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(percy));
		assertEquals(0, scn.GetWoundsOn(bard));

		scn.FreepsPass();
		scn.ShadowUseCardAction(warg);
		scn.ShadowChooseCard(volunteers);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(percy));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(percy));
		assertEquals(0, scn.GetWoundsOn(volunteers));

		// Vitality 3 with 2 wounds: Percy cannot exert, so the next wound is not his to prevent.
		scn.FreepsPass();
		scn.ShadowUseCardAction(warg2);
		scn.ShadowChooseCard(bard);
		assertEquals(1, scn.GetWoundsOn(bard));
		assertEquals(2, scn.GetWoundsOn(percy));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedForAWoundToPercyHimselfNorToAnAllyOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var percy = scn.GetFreepsCard("percy");
		var dain = scn.GetFreepsCard("dain");
		var aggressor = scn.GetShadowCard("aggressor");
		var warg = scn.GetShadowCard("warg");
		scn.MoveAlliesToTable(percy, dain);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, warg);
		assertAttachedTo(warg, aggressor);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetWoundsOn(percy));
		assertEquals(0, scn.GetWoundsOn(dain));
		scn.FreepsPass();
		scn.ShadowUseCardAction(warg);
		scn.ShadowChooseCard(percy);
		// Percy could pay, but the wound is his own.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(percy));
		assertEquals(1, scn.GetWoundsOn(percy));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.FreepsPass();
		scn.ShadowUseCardAction(warg);
		scn.ShadowChooseCard(dain);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(percy));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetWoundsOn(percy));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
