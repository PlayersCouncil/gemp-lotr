package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_144_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("patrol", "11_144");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion; Valiant is its whole text
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally; a fellowship ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; a skirmish ability used only on purpose
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit (a Man only beside three [rohan]
					// companions or Éowyn); his trigger answers a [rohan] Man exerting, and nobody exerts here
					put("merry", "103_85");
					// Rider's Mount: a [rohan] mount, "Bearer must be a [rohan] Man"; its text acts only in a skirmish
					put("mount", "4_287");
					put("mount2", "4_287");
					// Horse of Rohan: a mount a Man of any culture may bear; its text acts only in a skirmish
					put("horse", "4_283");
					// Heavy Chain: armor, no mount, "Bearer must be a Man"; its text is about overwhelming
					put("chain", "4_278");
					// Doom Drove Them: another [rohan] support-area condition; its abilities are used only on purpose
					put("doom", "8_86");
					put("doom2", "8_86");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 2 is Eastfold 4_331 (plains); site 3 is Barrows of Edoras 4_337 (a sanctuary, no plains).
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("patrol", "11_144");
					// Doom Drove Them: another [rohan] support-area condition; its abilities are used only on purpose
					put("doom", "8_86");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; Damage +1 is its whole text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; "When you play this minion, you may add (2)"
					// Goblin Armory: a [moria] support-area condition; its texts answer a [moria] weapon and a [moria] Orc's wound
					put("armory", "1_173");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 3 is Westemnet Plains 4_342: plains and a sanctuary.
	protected VirtualTableScenario GetWestemnetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("patrol", "11_144");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site3", "4_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BorderPatrolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Border Patrol
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: When you play this condition, spot a mounted [rohan] Man to add 3 [rohan] tokens here.<br><b>Response:</b> If a minion is played at a plains site, discard this condition or remove a [rohan] token from here to exert that minion.
		*/

		var scn = GetScenario();

		var patrol = scn.GetFreepsCard("patrol");

		assertEquals("Border Patrol", patrol.getBlueprint().getTitle());
		assertNull(patrol.getBlueprint().getSubtitle());
		assertTrue(patrol.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, patrol.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, patrol.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, patrol.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(patrol, Keyword.SUPPORT_AREA));
		assertEquals(1, patrol.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedBesideAMountedRohanManCompanionItTakesThreeTokensAndAnotherCardPlayedLaterAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetFreepsCard("patrol");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var doom = scn.GetFreepsCard("doom");
		var doom2 = scn.GetFreepsCard("doom2");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToSupportArea(doom);
		scn.MoveCardsToHand(patrol, doom2);

		scn.StartGame();
		scn.AddTokensToCard(doom, 1);
		assertEquals(guard, mount.getAttachedTo());
		assertEquals(1, scn.GetCultureTokensOn(doom));

		scn.FreepsPlayCard(patrol);

		// No "may": the tokens are added unasked, and on this card alone.
		assertInZone(Zone.SUPPORT, patrol);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetCultureTokensOn(patrol));
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another card played once this one is in play is not "this condition".
		scn.FreepsPlayCard(doom2);
		assertInZone(Zone.SUPPORT, doom2);
		assertEquals(3, scn.GetCultureTokensOn(patrol));
		assertEquals(0, scn.GetCultureTokensOn(doom2));
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMountedRohanManAllyIsEnoughToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetFreepsCard("patrol");
		var guma = scn.GetFreepsCard("guma");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guma, mount);
		scn.MoveCardsToHand(patrol);

		scn.StartGame();
		assertEquals(guma, mount.getAttachedTo());

		scn.FreepsPlayCard(patrol);

		assertInZone(Zone.SUPPORT, patrol);
		assertEquals(3, scn.GetCultureTokensOn(patrol));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithARohanManWhoIsNotMountedAMountedGondorManAndARohanHobbitNoTokensAreAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetFreepsCard("patrol");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		var boromir = scn.GetFreepsCard("boromir");
		var horse = scn.GetFreepsCard("horse");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(guard, boromir, merry);
		scn.AttachCardsTo(guard, chain);
		scn.AttachCardsTo(boromir, horse);
		scn.MoveCardsToHand(patrol);

		scn.StartGame();
		assertEquals(guard, chain.getAttachedTo());
		assertEquals(boromir, horse.getAttachedTo());
		// Two [rohan] companions and no Éowyn: Merry is a Hobbit.
		assertFalse(scn.IsRace(merry, Race.MAN));

		scn.FreepsPlayCard(patrol);

		// The Guard bears armor and no mount; Boromir is mounted but no [rohan] Man; Merry is no Man.
		assertInZone(Zone.SUPPORT, patrol);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(patrol));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMinionPlayedAtAPlainsSiteIsExertedForATokenWhenBothCostsCanBePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var patrol = scn.GetFreepsCard("patrol");
		scn.MoveCardsToSupportArea(patrol);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddTokensToCard(patrol, 2);
		assertInZone(Zone.SUPPORT, patrol);
		assertEquals(2, scn.GetCultureTokensOn(patrol));
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToHand(troop);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// Eastfold, site 2: plains.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.ShadowPlayCard(troop);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(patrol));
		scn.FreepsAcceptOptionalTrigger();
		// Both costs can be paid, so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		assertTrue(scn.FreepsChoiceAvailable("token from here"));
		scn.FreepsChooseOption("token from here");

		// The minion just played exerts; the one already in play does not.
		assertEquals(1, scn.GetCultureTokensOn(patrol));
		assertInZone(Zone.SUPPORT, patrol);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));

		// A response may be used again on the same play while its cost can be paid (CR 4.2 "response"): declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(patrol));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(patrol));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoTokenHereTheConditionIsDiscardedUnaskedToExertTheMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var patrol = scn.GetFreepsCard("patrol");
		var doom = scn.GetFreepsCard("doom");
		scn.MoveCardsToSupportArea(patrol, doom);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// The token lies on the other [rohan] condition, not here.
		scn.AddTokensToCard(doom, 1);
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertEquals(0, scn.GetCultureTokensOn(patrol));
		assertInZone(Zone.SUPPORT, patrol);
		scn.MoveCardsToHand(savage);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(savage);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(patrol));
		scn.FreepsAcceptOptionalTrigger();

		// The discard is the one cost that can be paid, and is carried out unasked.
		assertInZone(Zone.DISCARD, patrol);
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionOfVitalityOneIsExertedOnlyInNameAndLives() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var patrol = scn.GetFreepsCard("patrol");
		scn.MoveCardsToSupportArea(patrol);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddTokensToCard(patrol, 2);
		assertEquals(2, scn.GetCultureTokensOn(patrol));
		scn.MoveCardsToHand(runner);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(runner);
		// The response has a cost, and is offered though the Runner cannot exert.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(patrol));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseOption("token from here");

		// The cost is paid; an exhausted minion cannot be exerted, and a wound would have killed it.
		assertEquals(1, scn.GetCultureTokensOn(patrol));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);

		// Offered again on the same play while a token is left: declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(patrol));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(patrol));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AShadowCardThatIsNoMinionPlayedAtAPlainsSiteMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var patrol = scn.GetFreepsCard("patrol");
		scn.MoveCardsToSupportArea(patrol);

		var armory = scn.GetShadowCard("armory");

		scn.StartGame();
		scn.AddTokensToCard(patrol, 2);
		assertEquals(2, scn.GetCultureTokensOn(patrol));
		scn.MoveCardsToHand(armory);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.ShadowPlayCard(armory);

		assertInZone(Zone.SUPPORT, armory);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetCultureTokensOn(patrol));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionPlayedAtASiteThatIsNotPlainsMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var patrol = scn.GetFreepsCard("patrol");
		scn.MoveCardsToSupportArea(patrol);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AddTokensToCard(patrol, 2);
		assertEquals(2, scn.GetCultureTokensOn(patrol));
		scn.MoveCardsToHand(troop);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// Barrows of Edoras, site 3: a sanctuary, and no plains.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.ShadowPlayCard(troop);

		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetCultureTokensOn(patrol));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtAPlainsSanctuaryAMinionPlayedIsExertedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWestemnetScenario();

		var patrol = scn.GetFreepsCard("patrol");
		scn.MoveCardsToSupportArea(patrol);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AddTokensToCard(patrol, 2);
		assertEquals(2, scn.GetCultureTokensOn(patrol));
		scn.MoveCardsToHand(troop);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// Westemnet Plains, site 3: plains and a sanctuary.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.ShadowPlayCard(troop);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(patrol));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseOption("token from here");

		assertEquals(1, scn.GetCultureTokensOn(patrol));
		assertEquals(1, scn.GetWoundsOn(troop));

		// Offered again on the same play while a token is left: declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(patrol));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(patrol));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
